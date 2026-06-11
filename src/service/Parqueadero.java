package service;

import model.TipoVehiculo;
import util.AppConfig;
import util.ConexionDB;
import util.Sesion;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lógica de negocio y gestión operativa del sistema de parqueadero utilizando
 * MySQL.
 */
public class Parqueadero {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String REGEX_PLACA = "^[A-Z0-9]{5,7}$";

    public Parqueadero() {
        // Constructor limpio, persistencia administrada en MySQL
    }

    // ==========================================
    // INGRESO DE VEHÍCULO (Tipado Estricto)
    // ==========================================
    public void ingresarVehiculo(String placa, TipoVehiculo tipo) {
        if (placa == null || placa.isBlank()) {
            System.out.println("❌ Error: La placa introducida no es válida.");
            return;
        }

        String placaFormateada = placa.toUpperCase().trim();

        if (!placaFormateada.matches(REGEX_PLACA)) {
            System.out.println(
                    "❌ Error: El formato de la placa es inválido (Debe tener entre 5 y 7 caracteres alfanuméricos).");
            return;
        }

        if (tipo == null) {
            System.out.println("❌ Error: El tipo de vehículo es obligatorio.");
            return;
        }

        int capacidadActual = AppConfig.obtenerCapacidadMaxima();
        int ocupados = getVehiculosActivosCount();
        if (ocupados >= capacidadActual) {
            System.out.println("❌ Error: El parqueadero ha alcanzado su capacidad máxima (" + capacidadActual + ").");
            return;
        }

        String sqlCheck = "SELECT COUNT(*) FROM vehiculos_activos WHERE placa = ?";
        try (Connection con = ConexionDB.getConexion();
                PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {

            psCheck.setString(1, placaFormateada);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out
                            .println("❌ Error: El vehículo con placa " + placaFormateada + " ya se encuentra activo.");
                    return;
                }
            }

            // AJUSTE: Se agregó la columna 'usuario_responsable' a la consulta INSERT
            String sqlInsert = "INSERT INTO vehiculos_activos (placa, tipo_vehiculo, fecha_ingreso, usuario_responsable) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, placaFormateada);
                psInsert.setString(2, tipo.name());
                psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                psInsert.setString(4, Sesion.getUsuarioActual()); // AJUSTE: Captura el usuario de la sesión

                psInsert.executeUpdate();
                System.out.println("✅ Vehículo registrado exitosamente por: " + Sesion.getUsuarioActual());
                System.out.println("🚗 Espacios ocupados: " + (ocupados + 1));
                System.out.println("🅿️ Espacios disponibles: " + (capacidadActual - (ocupados + 1)));
            }

        } catch (SQLException e) {
            System.out.println("❌ Error de base de datos al ingresar vehículo: " + e.getMessage());
        }
    }

    // ==========================================
    // LISTAR VEHÍCULOS ACTIVOS
    // ==========================================
    public void mostrarVehiculos() {
        String sql = "SELECT placa, tipo_vehiculo, fecha_ingreso FROM vehiculos_activos";

        try (Connection con = ConexionDB.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("ℹ️ No hay vehículos activos dentro del parqueadero en este momento.");
                return;
            }

            System.out.println("\n===== VEHÍCULOS ACTIVOS =====");
            int count = 0;
            while (rs.next()) {
                count++;
                String placa = rs.getString("placa");
                String tipoRaw = rs.getString("tipo_vehiculo");
                LocalDateTime ingreso = rs.getTimestamp("fecha_ingreso").toLocalDateTime();

                String tipoPresentacion = TipoVehiculo.esValido(tipoRaw)
                        ? TipoVehiculo.fromString(tipoRaw).toString()
                        : tipoRaw;

                System.out.printf("🚗 [%s] Tipo: %-6s | Ingreso: %s%n",
                        placa, tipoPresentacion, ingreso.format(FORMATO_FECHA));
            }

            int capacidadActual = AppConfig.obtenerCapacidadMaxima();
            System.out.println("\n-------------------------------------");
            System.out.println("Capacidad total : " + capacidadActual);
            System.out.println("Ocupados        : " + count);
            System.out.println("Disponibles     : " + (capacidadActual - count));
            System.out.println("-------------------------------------");

        } catch (SQLException e) {
            System.out.println("❌ Error al listar vehículos desde la DB: " + e.getMessage());
        }
    }

    // ==========================================
    // RETIRAR VEHÍCULO (FACTURACIÓN)
    // ==========================================
    public void retirarVehiculo(String placa) {
        if (placa == null || placa.isBlank())
            return;
        String placaBusqueda = placa.toUpperCase().trim();

        String sqlBuscar = "SELECT v.tipo_vehiculo, v.fecha_ingreso, t.valor_hora " +
                "FROM vehiculos_activos v " +
                "JOIN tarifas t ON v.tipo_vehiculo = t.tipo_vehiculo " +
                "WHERE v.placa = ?";

        try (Connection con = ConexionDB.getConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                psBuscar.setString(1, placaBusqueda);

                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ Error: Vehículo con placa '" + placaBusqueda + "' no encontrado.");
                        con.rollback();
                        return;
                    }

                    String tipoRaw = rs.getString("tipo_vehiculo");
                    LocalDateTime entrada = rs.getTimestamp("fecha_ingreso").toLocalDateTime();
                    double tarifaHora = rs.getDouble("valor_hora");

                    LocalDateTime salida = LocalDateTime.now();
                    long minutos = Duration.between(entrada, salida).toMinutes();
                    long horasFacturadas = Math.max(1, (long) Math.ceil(minutos / 60.0));
                    double totalAPagar = horasFacturadas * tarifaHora;

                    String tipoPresentacion = TipoVehiculo.esValido(tipoRaw)
                            ? TipoVehiculo.fromString(tipoRaw).toString()
                            : tipoRaw;

                    System.out.println("\n=====================================");
                    System.out.println("           TICKET DE SALIDA          ");
                    System.out.println("=====================================");
                    System.out.println("Placa        : " + placaBusqueda);
                    System.out.println("Tipo         : " + tipoPresentacion);
                    System.out.println("Entrada      : " + entrada.format(FORMATO_FECHA));
                    System.out.println("Salida       : " + salida.format(FORMATO_FECHA));
                    System.out.println("Tiempo real  : " + minutos + " minuto(s)");
                    System.out.println("Horas cobro  : " + horasFacturadas);
                    System.out.println("Tarifa/Hora  : " + formatoMoneda(tarifaHora));
                    System.out.println("-------------------------------------");
                    System.out.println("TOTAL A PAGAR: " + formatoMoneda(totalAPagar));
                    System.out.println("Usuario      : " + Sesion.getUsuarioActual());
                    System.out.println("=====================================");

                    // Se añade el campo usuario_responsable en la inserción al historial
                    String sqlHistorial = "INSERT INTO historial_movimientos (placa, tipo_vehiculo, fecha_ingreso, fecha_salida, minutos_totales, total_pagado, usuario_responsable) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement psHistorial = con.prepareStatement(sqlHistorial)) {
                        psHistorial.setString(1, placaBusqueda);
                        psHistorial.setString(2, tipoRaw);
                        psHistorial.setTimestamp(3, Timestamp.valueOf(entrada));
                        psHistorial.setTimestamp(4, Timestamp.valueOf(salida));
                        psHistorial.setLong(5, minutos);
                        psHistorial.setDouble(6, totalAPagar);
                        psHistorial.setString(7, Sesion.getUsuarioActual()); // Inyectamos el usuario de la sesión
                        psHistorial.executeUpdate();
                    }

                    String sqlDelete = "DELETE FROM vehiculos_activos WHERE placa = ?";
                    try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                        psDelete.setString(1, placaBusqueda);
                        psDelete.executeUpdate();
                    }

                    con.commit();
                    System.out.println(
                            "\n✅ Vehículo retirado y registrado en el historial por: " + Sesion.getUsuarioActual());
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error transaccional al retirar el vehículo: " + e.getMessage());
        }
    }

    // ==========================================
    // VER HISTORIAL COMPLETO
    // ==========================================
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
                String tipoRaw = rs.getString("tipo_vehiculo");
                String tipoPresentacion = TipoVehiculo.esValido(tipoRaw) ? TipoVehiculo.fromString(tipoRaw).toString()
                        : tipoRaw;

                System.out.printf("📄 ID: %-3d | Placa: %-7s | Tipo: %-6s | Entrada: %s | Salida: %s | Total: %s%n",
                        rs.getInt("id"),
                        rs.getString("placa"),
                        tipoPresentacion,
                        rs.getTimestamp("fecha_ingreso").toLocalDateTime().format(FORMATO_FECHA),
                        rs.getTimestamp("fecha_salida").toLocalDateTime().format(FORMATO_FECHA),
                        formatoMoneda(rs.getDouble("total_pagado")));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al leer el historial: " + e.getMessage());
        }
    }

    // ==========================================
    // BUSCAR VEHÍCULO ACTIVO
    // ==========================================
    public void buscarVehiculo(String placa) {
        String sql = "SELECT * FROM vehiculos_activos WHERE placa = ?";
        try (Connection con = ConexionDB.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, placa.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tipoRaw = rs.getString("tipo_vehiculo");
                    String tipoPresentacion = TipoVehiculo.esValido(tipoRaw)
                            ? TipoVehiculo.fromString(tipoRaw).toString()
                            : tipoRaw;

                    System.out.println("\n===== VEHÍCULO LOCALIZADO =====");
                    System.out.printf("🚗 Placa: %s | Tipo: %s | Ingreso: %s%n",
                            rs.getString("placa"),
                            tipoPresentacion,
                            rs.getTimestamp("fecha_ingreso").toLocalDateTime().format(FORMATO_FECHA));
                } else {
                    System.out.println("❌ El vehículo solicitado no se encuentra dentro del parqueadero.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al buscar vehículo: " + e.getMessage());
        }
    }

    // ==========================================
    // ESTADO DEL PARQUEADERO
    // ==========================================
    public void mostrarEstado() {
        int capacidadActual = AppConfig.obtenerCapacidadMaxima();
        int ocupados = getVehiculosActivosCount();
        System.out.println("\n===== ESTADO DEL PARQUEADERO =====");
        System.out.println("Capacidad total : " + capacidadActual);
        System.out.println("Ocupados        : " + ocupados);
        System.out.println("Disponibles     : " + (capacidadActual - ocupados));
    }

    // ==========================================
    // METRICAS Y ESTADÍSTICAS EN TIEMPO REAL
    // ==========================================
    public void mostrarEstadisticas() {
        String sqlActivos = "SELECT tipo_vehiculo, COUNT(*) FROM vehiculos_activos GROUP BY tipo_vehiculo";
        String sqlHistoricos = "SELECT tipo_vehiculo, COUNT(*), SUM(total_pagado) FROM historial_movimientos GROUP BY tipo_vehiculo";

        long carrosActivos = 0, motosActivas = 0;
        long carrosHistoricos = 0, motosHistoricas = 0;
        double ingresosTotales = 0;

        try (Connection con = ConexionDB.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sqlActivos); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString(1).equalsIgnoreCase("CARRO"))
                        carrosActivos = rs.getLong(2);
                    if (rs.getString(1).equalsIgnoreCase("MOTO"))
                        motosActivas = rs.getLong(2);
                }
            }
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
            System.out.println("Carros procesados      : " + carrosHistoricos);
            System.out.println("Motos procesadas       : " + motosHistoricas);
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
        if (placa == null || placa.isBlank())
            return;
        String sql = "SELECT * FROM historial_movimientos WHERE placa = ? ORDER BY fecha_salida DESC";

        try (Connection con = ConexionDB.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, placa.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("❌ No se encontraron registros de salidas históricos para la placa: "
                            + placa.toUpperCase());
                    return;
                }

                System.out.println("\n===== REGISTROS ENCONTRADOS =====");
                while (rs.next()) {
                    String tipoRaw = rs.getString("tipo_vehiculo");
                    String tipoPresentacion = TipoVehiculo.esValido(tipoRaw)
                            ? TipoVehiculo.fromString(tipoRaw).toString()
                            : tipoRaw;

                    System.out.printf("📄 ID: %d | Tipo: %s | Entrada: %s | Salida: %s | Pagado: %s%n",
                            rs.getInt("id"),
                            tipoPresentacion,
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
    // REPORTES FINANCIEROS AVANZADOS
    // ==========================================
    public void mostrarReporteFinanciero() {
        String sqlHoy = "SELECT COALESCE(SUM(total_pagado), 0) FROM historial_movimientos WHERE DATE(fecha_salida) = CURDATE()";
        String sqlMes = "SELECT COALESCE(SUM(total_pagado), 0) FROM historial_movimientos WHERE MONTH(fecha_salida) = MONTH(CURDATE()) AND YEAR(fecha_salida) = YEAR(CURDATE())";
        String sqlTotal = "SELECT COALESCE(SUM(total_pagado), 0), COUNT(*), COALESCE(AVG(total_pagado), 0) FROM historial_movimientos";

        try (Connection con = ConexionDB.getConexion()) {
            double hoy = 0, mes = 0, total = 0, promedio = 0;
            int transacciones = 0;

            try (PreparedStatement ps = con.prepareStatement(sqlHoy); ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    hoy = rs.getDouble(1);
            }
            try (PreparedStatement ps = con.prepareStatement(sqlMes); ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    mes = rs.getDouble(1);
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

    // ==========================================
    // RANKING VEHÍCULOS FRECUENTES (TOP 10)
    // ==========================================
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
                String barraVisual = "■".repeat(Math.min(15, Math.max(1, visitas)));

                System.out.printf("  #%02d  | %-10s | %-12s | %-15s%n",
                        puesto++, placa, visitas + " visitas", barraVisual);
            }
            System.out.println("=======================================================");

        } catch (SQLException e) {
            System.out.println("❌ Error al renderizar el dashboard: " + e.getMessage());
        }
    }

    private int getVehiculosActivosCount() {
        String sql = "SELECT COUNT(*) FROM vehiculos_activos";
        try (Connection con = ConexionDB.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Error al contar ocupación: " + e.getMessage());
        }
        return 0;
    }

    private String formatoMoneda(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }

    // ==========================================
    // VALIDACIÓN DE LOGINES CONTRA MYSQL
    // ==========================================
    public boolean validarLogin(String username, String password) {
        // Modificado para usar exactamente las columnas correctas de tu tabla usuarios
        String sql = "SELECT rol FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = ConexionDB.getConexion();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol");
                    Sesion.iniciarSesion(username, rol);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en el proceso de autenticación: " + e.getMessage());
        }
        return false;
    }
}