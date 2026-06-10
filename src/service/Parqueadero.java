package service;

import model.TipoVehiculo;
import model.Vehiculo;
import util.ConexionDB;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lógica de negocio y gestión operativa del sistema de parqueadero utilizando MySQL.
 */
public class Parqueadero {

    private static final int CAPACIDAD_MAXIMA = 20;
    private static final DateTimeFormatter FORMATO_FECHA = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // El constructor queda limpio, ya no necesita cargar listas a RAM
    public Parqueadero() {
        // La base de datos y tarifas ya se manejan directamente en MySQL
    }

    // =========================
    // INGRESO DE VEHÍCULO
    // =========================
    public void ingresarVehiculo(String placa, String tipo) {
        if (placa == null || placa.isBlank()) {
            System.out.println("❌ Error: La placa introducida no es válida.");
            return;
        }

        String placaFormateada = placa.toUpperCase().trim();

        // Validar capacidad actual consultando la BD
        int ocupados = getVehiculosActivosCount();
        if (ocupados >= CAPACIDAD_MAXIMA) {
            System.out.println("❌ Error: El parqueadero ha alcanzado su capacidad máxima (" + CAPACIDAD_MAXIMA + ").");
            return;
        }

        // Validar duplicados directamente en la BD
        String sqlCheck = "SELECT COUNT(*) FROM vehiculos_activos WHERE placa = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
            
            psCheck.setString(1, placaFormateada);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ Error: El vehículo con placa " + placaFormateada + " ya se encuentra activo en el parqueadero.");
                    return;
                }
            }

            // Inserción indexada en MySQL
            String sqlInsert = "INSERT INTO vehiculos_activos (placa, tipo_vehiculo, fecha_ingreso) VALUES (?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, placaFormateada);
                psInsert.setString(2, tipo.toUpperCase());
                psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                
                psInsert.executeUpdate();
                System.out.println("✅ Vehículo registrado exitosamente en MySQL.");
                System.out.println("🚗 Espacios ocupados: " + (ocupados + 1));
                System.out.println("🅿️ Espacios disponibles: " + (CAPACIDAD_MAXIMA - (ocupados + 1)));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error de base de datos al ingresar vehículo: " + e.getMessage());
        }
    }

    // =========================
    // LISTAR VEHÍCULOS ACTIVOS
    // =========================
    public void mostrarVehiculos() {
        String sql = "SELECT placa, tipo_vehiculo, fecha_ingreso FROM vehiculos_activos";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) { // Verifica si la consulta regresó vacía
                System.out.println("ℹ️ No hay vehículos activos dentro del parqueadero en este momento.");
                return;
            }

            System.out.println("\n===== VEHÍCULOS ACTIVOS =====");
            int count = 0;
            while (rs.next()) {
                count++;
                String placa = rs.getString("placa");
                String tipo = rs.getString("tipo_vehiculo");
                LocalDateTime ingreso = rs.getTimestamp("fecha_ingreso").toLocalDateTime();
                
                System.out.printf("🚗 [%s] Tipo: %-6s | Ingreso: %s%n", 
                        placa, tipo, ingreso.format(FORMATO_FECHA));
            }

            System.out.println("\n-------------------------------------");
            System.out.println("Capacidad total : " + CAPACIDAD_MAXIMA);
            System.out.println("Ocupados        : " + count);
            System.out.println("Disponibles     : " + (CAPACIDAD_MAXIMA - count));
            System.out.println("-------------------------------------");

        } catch (SQLException e) {
            System.out.println("❌ Error al listar vehículos desde la DB: " + e.getMessage());
        }
    }

    // =========================
    // RETIRAR VEHÍCULO (FACTURACIÓN)
    // =========================
    public void retirarVehiculo(String placa) {
        if (placa == null || placa.isBlank()) return;
        String placaBusqueda = placa.toUpperCase().trim();

        // 1. Buscar el vehículo y traer el valor_hora cruzando tablas (JOIN)
        String sqlBuscar = "SELECT v.tipo_vehiculo, v.fecha_ingreso, t.valor_hora " +
                           "FROM vehiculos_activos v " +
                           "JOIN tarifas t ON v.tipo_vehiculo = t.tipo_vehiculo " +
                           "WHERE v.placa = ?";

        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false); // Iniciamos Transacción Atómica para evitar inconsistencias

            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                psBuscar.setString(1, placaBusqueda);
                
                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ Error: Vehículo con placa '" + placaBusqueda + "' no encontrado.");
                        con.rollback();
                        return;
                    }

                    String tipo = rs.getString("tipo_vehiculo");
                    LocalDateTime entrada = rs.getTimestamp("fecha_ingreso").toLocalDateTime();
                    double tarifaHora = rs.getDouble("valor_hora");

                    LocalDateTime salida = LocalDateTime.now();
                    long minutos = Duration.between(entrada, salida).toMinutes();
                    long horasFacturadas = Math.max(1, (long) Math.ceil(minutos / 60.0));
                    double totalAPagar = horasFacturadas * tarifaHora;

                    // Imprimir Ticket Estético
                    System.out.println("\n=====================================");
                    System.out.println("           TICKET DE SALIDA          ");
                    System.out.println("=====================================");
                    System.out.println("Placa        : " + placaBusqueda);
                    System.out.println("Tipo         : " + tipo);
                    System.out.println("Entrada      : " + entrada.format(FORMATO_FECHA));
                    System.out.println("Salida       : " + salida.format(FORMATO_FECHA));
                    System.out.println("Tiempo real  : " + minutos + " minuto(s)");
                    System.out.println("Horas cobro  : " + horasFacturadas);
                    System.out.println("Tarifa/Hora  : " + formatoMoneda(tarifaHora));
                    System.out.println("-------------------------------------");
                    System.out.println("TOTAL A PAGAR: " + formatoMoneda(totalAPagar));
                    System.out.println("=====================================");

                    // 2. Insertar movimiento en el historial definitivo
                    String sqlHistorial = "INSERT INTO historial_movimientos (placa, tipo_vehiculo, fecha_ingreso, fecha_salida, minutos_totales, total_pagado) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psHistorial = con.prepareStatement(sqlHistorial)) {
                        psHistorial.setString(1, placaBusqueda);
                        psHistorial.setString(2, tipo);
                        psHistorial.setTimestamp(3, Timestamp.valueOf(entrada));
                        psHistorial.setTimestamp(4, Timestamp.valueOf(salida));
                        psHistorial.setLong(5, minutos);
                        psHistorial.setDouble(6, totalAPagar);
                        psHistorial.executeUpdate();
                    }

                    // 3. Eliminar de activos
                    String sqlDelete = "DELETE FROM vehiculos_activos WHERE placa = ?";
                    try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                        psDelete.setString(1, placaBusqueda);
                        psDelete.executeUpdate();
                    }

                    con.commit(); // Confirmamos todos los pasos juntos de forma segura
                    System.out.println("\n✅ Vehículo retirado y registrado en el historial de MySQL con éxito.");
                }
            } catch (SQLException e) {
                con.rollback(); // Deshacer si algo falla a mitad de camino
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error transaccional al retirar el vehículo: " + e.getMessage());
        }
    }

    // =========================
    // VER HISTORIAL COMPLETO
    // =========================
    public void mostrarHistorial() {
        String sql = "SELECT * FROM historial_movimientos ORDER BY id DESC";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("ℹ️ No existen movimientos históricos registrados en el sistema.");
                return;
            }

            System.out.println("\n===== REPORTE HISTÓRICO GENERAL =====");
            while (rs.next()) {
                System.out.printf("📄 ID: %-3d | Placa: %-7s | Tipo: %-6s | Entrada: %s | Salida: %s | Total: %s%n",
                        rs.getInt("id"),
                        rs.getString("placa"),
                        rs.getString("tipo_vehiculo"),
                        rs.getTimestamp("fecha_ingreso").toLocalDateTime().format(FORMATO_FECHA),
                        rs.getTimestamp("fecha_salida").toLocalDateTime().format(FORMATO_FECHA),
                        formatoMoneda(rs.getDouble("total_pagado"))
                );
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al leer el historial: " + e.getMessage());
        }
    }

    // =========================
    // BUSCAR VEHÍCULO ACTIVO
    // =========================
    public void buscarVehiculo(String placa) {
        String sql = "SELECT * FROM vehiculos_activos WHERE placa = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, placa.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n===== VEHÍCULO LOCALIZADO =====");
                    System.out.printf("🚗 Placa: %s | Tipo: %s | Ingreso: %s%n",
                            rs.getString("placa"),
                            rs.getString("tipo_vehiculo"),
                            rs.getTimestamp("fecha_ingreso").toLocalDateTime().format(FORMATO_FECHA));
                } else {
                    System.out.println("❌ El vehículo solicitado no se encuentra dentro del parqueadero.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al buscar vehículo: " + e.getMessage());
        }
    }

    // =========================
    // ESTADO DEL PARQUEADERO
    // =========================
    public void mostrarEstado() {
        int ocupados = getVehiculosActivosCount();
        System.out.println("\n===== ESTADO DEL PARQUEADERO =====");
        System.out.println("Capacidad total : " + CAPACIDAD_MAXIMA);
        System.out.println("Ocupados        : " + ocupados);
        System.out.println("Disponibles     : " + (CAPACIDAD_MAXIMA - ocupados));
    }

    // ==========================================
    // METRICAS Y ESTADÍSTICAS EN TIEMPO REAL VIA SQL
    // ==========================================
    public void mostrarEstadisticas() {
        String sqlActivos = "SELECT tipo_vehiculo, COUNT(*) FROM vehiculos_activos GROUP BY tipo_vehiculo";
        String sqlHistoricos = "SELECT tipo_vehiculo, COUNT(*), SUM(total_pagado) FROM historial_movimientos GROUP BY tipo_vehiculo";
        
        long carrosActivos = 0, motosActivas = 0;
        long carrosHistoricos = 0, motosHistoricas = 0;
        double ingresosTotales = 0;

        try (Connection con = ConexionDB.getConexion()) {
            // Procesar activos
            try (PreparedStatement ps = con.prepareStatement(sqlActivos); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString(1).equalsIgnoreCase("CARRO")) carrosActivos = rs.getLong(2);
                    if (rs.getString(1).equalsIgnoreCase("MOTO")) motosActivas = rs.getLong(2);
                }
            }
            // Procesar históricos
            try (PreparedStatement ps = con.prepareStatement(sqlHistoricos); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString(1).equalsIgnoreCase("CARRO")) {
                        carrosHistoricos = rs.getLong(2);
                        ingresosTotales += rs.getDouble(3);
                    }
                    if (rs.getString(1).equalsIgnoreCase("MOTO")) {
                        motosHistoricas = rs.getLong(2);
                        ingresosTotales += rs.getDouble(3);
                    }
                }
            }

            System.out.println("\n===== METRICAS Y ESTADÍSTICAS =====");
            System.out.println("Vehículos activos      : " + (carrosActivos + motosActivas));
            System.out.println("Movimientos históricos : " + (carrosHistoricos + motosHistoricas));
            System.out.println("\n----- OCUPACIÓN EN TIEMPO REAL -----");
            System.out.println("Carros activos         : " + carrosActivos);
            System.out.println("Motos activas          : " + motosActivas);
            System.out.println("\n----- FLUJO HISTÓRICO ACUMULADO -----");
            System.out.println("Carros processed       : " + carrosHistoricos);
            System.out.println("Motos processed        : " + motosHistoricas);
            System.out.println("\n----- TOTAL RECAUDADO -----");
            System.out.println("Ingresos globales      : " + formatoMoneda(ingresosTotales));

        } catch (SQLException e) {
            System.out.println("❌ Error al procesar métricas: " + e.getMessage());
        }
    }

    // ==========================================
    // BUSCAR FILTRO POR PLACA EN HISTORIAL
    // ==========================================
    public void buscarHistorialPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) return;
        String sql = "SELECT * FROM historial_movimientos WHERE placa = ? ORDER BY fecha_salida DESC";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, placa.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("❌ No se encontraron registros de salidas históricos para la placa: " + placa.toUpperCase());
                    return;
                }
                
                System.out.println("\n===== REGISTROS ENCONTRADOS =====");
                while (rs.next()) {
                    System.out.printf("📄 ID: %d | Tipo: %s | Entrada: %s | Salida: %s | Pagado: %s%n",
                            rs.getInt("id"),
                            rs.getString("tipo_vehiculo"),
                            rs.getTimestamp("fecha_ingreso").toLocalDateTime().format(FORMATO_FECHA),
                            rs.getTimestamp("fecha_salida").toLocalDateTime().format(FORMATO_FECHA),
                            formatoMoneda(rs.getDouble("total_pagado")));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al filtrar historial: " + e.getMessage());
        }
    }

    // ==========================================
    // REPORTES FINANCIEROS AVANZADOS (SQL PURO)
    // ==========================================
    public void mostrarReporteFinanciero() {
        String sqlHoy = "SELECT COALESCE(SUM(total_pagado), 0) FROM historial_movimientos WHERE DATE(fecha_salida) = CURDATE()";
        String sqlMes = "SELECT COALESCE(SUM(total_pagado), 0) FROM historial_movimientos WHERE MONTH(fecha_salida) = MONTH(CURDATE()) AND YEAR(fecha_salida) = YEAR(CURDATE())";
        String sqlTotal = "SELECT COALESCE(SUM(total_pagado), 0), COUNT(*), COALESCE(AVG(total_pagado), 0) FROM historial_movimientos";

        try (Connection con = ConexionDB.getConexion()) {
            double hoy = 0, mes = 0, total = 0, promedio = 0;
            int transacciones = 0;

            try (PreparedStatement ps = con.prepareStatement(sqlHoy); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) hoy = rs.getDouble(1);
            }
            try (PreparedStatement ps = con.prepareStatement(sqlMes); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) mes = rs.getDouble(1);
            }
            try (PreparedStatement ps = con.prepareStatement(sqlTotal); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                    transacciones = rs.getInt(2);
                    promedio = rs.getDouble(3);
                }
            }

            System.out.println("\n===== REPORTE FINANCIERO DE CAJA =====");
            System.out.println("Ingresos hoy      : " + formatoMoneda(hoy));
            System.out.println("Ingresos este mes : " + formatoMoneda(mes));
            System.out.println("Ingresos totales  : " + formatoMoneda(total));
            System.out.println("Ticket promedio   : " + formatoMoneda(promedio));
            System.out.println("Total transacciones: " + transacciones);

        } catch (SQLException e) {
            System.out.println("❌ Error al generar reporte financiero: " + e.getMessage());
        }
    }

    // =======================================================
    // RANKING VEHÍCULOS FRECUENTES (TOP 10 EN UNA SOLA AGREGACIÓN SQL)
    // =======================================================
    public void mostrarRankingVehiculos() {
        String sql = "SELECT placa, COUNT(*) as visitas FROM historial_movimientos GROUP BY placa ORDER BY visitas DESC, placa ASC LIMIT 10";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("\n❌ No existen movimientos registrados para generar el ranking.");
                return;
            }

            System.out.println("\n=======================================================");
            System.out.println("📊        DASHBOARD: TOP 10 VEHÍCULOS FRECUENTES       ");
            System.out.println("=======================================================");
            System.out.printf("%-6s | %-10s | %-12s | %-15s%n", "PUESTO", "PLACA", "VISITAS", "FRECUENCIA VISUAL");
            System.out.println("-------------------------------------------------------");

            int puesto = 1;
            while (rs.next()) {
                String placa = rs.getString("placa");
                int visitas = rs.getInt("visitas");
                
                // Generación de barra proporcional simple basada en visitas
                String barraVisual = "■".repeat(Math.min(15, Math.max(1, visitas)));

                System.out.printf("  #%02d  | %-10s | %-12s | %-15s%n", 
                        puesto++, placa, visitas + " visitas", barraVisual);
            }
            System.out.println("=======================================================");

        } catch (SQLException e) {
            System.out.println("❌ Error al renderizar el dashboard: " + e.getMessage());
        }
    }

    // Métodos auxiliares de consulta simplificada
    private int getVehiculosActivosCount() {
        String sql = "SELECT COUNT(*) FROM vehiculos_activos";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Error al contar ocupación: " + e.getMessage());
        }
        return 0;
    }

    private String formatoMoneda(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }
}