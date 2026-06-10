// Ruta: src/service/Parqueadero.java

package service;

import model.Movimiento;
import model.TipoVehiculo;
import model.Vehiculo;
import util.FileUtil;
import util.Tarifas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Lógica de negocio y gestión operativa del sistema de parqueadero.
 */
public class Parqueadero {

    // =========================
    // CONFIGURACIÓN CENTRAL
    // =========================
    private static final int CAPACIDAD_MAXIMA = 20;

    // =========================
    // FORMATO FECHAS GLOBAL (UI)
    // =========================
    private static final DateTimeFormatter FORMATO_FECHA = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================
    // COLECCIONES EN MEMORIA RAM
    // =========================
    private final ArrayList<Vehiculo> vehiculos = new ArrayList<>();
    private final ArrayList<Movimiento> historial = new ArrayList<>();

    // =========================
    // CONSTRUCTOR
    // =========================
    public Parqueadero() {
        // Garantizar infraestructura de persistencia antes de mapear datos
        FileUtil.ensureDataFolder();
        Tarifas.cargarTarifas();
        
        // Carga controlada filtrando posibles registros corruptos
        this.vehiculos.addAll(FileUtil.cargarVehiculos());
        this.historial.addAll(FileUtil.cargarHistorial());
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

        // Validación preventiva del Enum para blindar el flujo de entrada
        TipoVehiculo tipoVehiculo;
        try {
            tipoVehiculo = TipoVehiculo.fromString(tipo);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
            return;
        }

        if (vehiculos.size() >= CAPACIDAD_MAXIMA) {
            System.out.println("❌ Error: El parqueadero ha alcanzado su capacidad máxima (" + CAPACIDAD_MAXIMA + ").");
            return;
        }

        // Validación de duplicados en caliente
        boolean yaExiste = vehiculos.stream()
                .anyMatch(v -> v.getPlaca().equalsIgnoreCase(placaFormateada));

        if (yaExiste) {
            System.out.println("❌ Error: El vehículo con placa " + placaFormateada + " ya se encuentra activo en el parqueadero.");
            return;
        }

        // Instanciación y persistencia inmediata
        Vehiculo nuevo = new Vehiculo(placaFormateada, tipoVehiculo);
        vehiculos.add(nuevo);
        guardarDatos(); // Mantiene sincronizado el archivo de texto al instante

        System.out.println("✅ Vehículo registrado exitosamente.");
        System.out.println("🚗 Espacios ocupados: " + vehiculos.size());
        System.out.println("🅿️ Espacios disponibles: " + getEspaciosDisponibles());
    }

    // =========================
    // LISTAR VEHÍCULOS ACTIVOS
    // =========================
    public void mostrarVehiculos() {
        if (vehiculos.isEmpty()) {
            System.out.println("ℹ️ No hay vehículos activos dentro del parqueadero en este momento.");
            return;
        }

        System.out.println("\n===== VEHÍCULOS ACTIVOS =====");
        vehiculos.forEach(Vehiculo::mostrarInformacion);

        System.out.println("\n-------------------------------------");
        System.out.println("Capacidad total : " + CAPACIDAD_MAXIMA);
        System.out.println("Ocupados        : " + vehiculos.size());
        System.out.println("Disponibles     : " + getEspaciosDisponibles());
        System.out.println("-------------------------------------");
    }

    // ==========================================
    // CÁLCULO DE HORAS COBRADAS (ARREDONDAMIENTO CEIL)
    // ==========================================
    private long calcularHorasFacturadas(long minutos) {
        return Math.max(1, (long) Math.ceil(minutos / 60.0));
    }

    // =========================
    // RETIRAR VEHÍCULO (FACTURACIÓN)
    // =========================
    public void retirarVehiculo(String placa) {
        Vehiculo vehiculo = buscar(placa);

        if (vehiculo == null) {
            System.out.println("❌ Error: Vehículo con placa '" + placa.toUpperCase() + "' no encontrado.");
            return;
        }

        LocalDateTime salida = LocalDateTime.now();
        Duration duracion = Duration.between(vehiculo.getHoraEntrada(), salida);
        long minutos = duracion.toMinutes();
        long horasFacturadas = calcularHorasFacturadas(minutos);

        // Inyección dinámica de tarifas reales cargadas desde AppConfig.TARIFAS_FILE
        double tarifaHora = (vehiculo.getTipo() == TipoVehiculo.MOTO)
                ? Tarifas.getTarifaMoto()
                : Tarifas.getTarifaCarro();

        double totalAPagar = horasFacturadas * tarifaHora;

        // Generación estética del ticket de salida
        System.out.println("\n=====================================");
        System.out.println("           TICKET DE SALIDA          ");
        System.out.println("=====================================");
        System.out.println("Placa        : " + vehiculo.getPlaca());
        System.out.println("Tipo         : " + vehiculo.getTipo()); // Llama al toString() estético
        System.out.println("Entrada      : " + vehiculo.getHoraEntrada().format(FORMATO_FECHA));
        System.out.println("Salida       : " + salida.format(FORMATO_FECHA));
        System.out.println("Tiempo real  : " + minutos + " minuto(s)");
        System.out.println("Horas cobro  : " + horasFacturadas);
        System.out.println("Tarifa/Hora  : " + formatoMoneda(tarifaHora));
        System.out.println("-------------------------------------");
        System.out.println("TOTAL A PAGAR: " + formatoMoneda(totalAPagar));
        System.out.println("=====================================");

        // Consolidación de datos históricos
        Movimiento movimiento = new Movimiento(
                vehiculo.getPlaca(),
                vehiculo.getTipo(),
                vehiculo.getHoraEntrada(),
                salida,
                totalAPagar
        );

        historial.add(movimiento);
        vehiculos.remove(vehiculo);
        guardarDatos(); // Transacción persistida: Limpia vehiculos.txt y añade a historial.txt

        System.out.println("\n✅ Vehículo retirado y registrado en el historial con éxito.");
    }

    // =========================
    // VER HISTORIAL COMPLETO
    // =========================
    public void mostrarHistorial() {
        if (historial.isEmpty()) {
            System.out.println("ℹ️ No existen movimientos históricos registrados en el sistema.");
            return;
        }
        System.out.println("\n===== REPORTE HISTÓRICO GENERAL =====");
        historial.forEach(Movimiento::mostrarInformacion);
    }

    // =========================
    // BUSCAR VEHÍCULO ACTIVO
    // =========================
    public void buscarVehiculo(String placa) {
        Vehiculo vehiculo = buscar(placa);

        if (vehiculo == null) {
            System.out.println("❌ El vehículo solicitado no se encuentra dentro del parqueadero.");
            return;
        }

        System.out.println("\n===== VEHÍCULO LOCALIZADO =====");
        vehiculo.mostrarInformacion();
    }

    // =========================
    // ESTADO DEL PARQUEADERO
    // =========================
    public void mostrarEstado() {
        System.out.println("\n===== ESTADO DEL PARQUEADERO =====");
        System.out.println("Capacidad total : " + CAPACIDAD_MAXIMA);
        System.out.println("Ocupados        : " + vehiculos.size());
        System.out.println("Disponibles     : " + getEspaciosDisponibles());
    }

    private int getEspaciosDisponibles() {
        return CAPACIDAD_MAXIMA - vehiculos.size();
    }

    // Operación funcional reusable de búsqueda
    private Vehiculo buscar(String placa) {
        if (placa == null) return null;
        return vehiculos.stream()
                .filter(v -> v.getPlaca().equalsIgnoreCase(placa.trim()))
                .findFirst()
                .orElse(null);
    }

    // ==========================================
    // ESTADÍSTICAS OPERATIVAS (OPTIMIZADO STREAMS)
    // ==========================================
    public void mostrarEstadisticas() {
        long carrosActivos = vehiculos.stream().filter(v -> v.getTipo() == TipoVehiculo.CARRO).count();
        long motosActivas = vehiculos.stream().filter(v -> v.getTipo() == TipoVehiculo.MOTO).count();

        long carrosHistoricos = historial.stream().filter(m -> m.getTipo() == TipoVehiculo.CARRO).count();
        long motosHistoricas = historial.stream().filter(m -> m.getTipo() == TipoVehiculo.MOTO).count();
        
        double ingresosTotales = historial.stream().mapToDouble(Movimiento::getTotal).sum();

        System.out.println("\n===== METRICAS Y ESTADÍSTICAS =====");
        System.out.println("Vehículos activos      : " + vehiculos.size());
        System.out.println("Movimientos históricos : " + historial.size());

        System.out.println("\n----- OCUPACIÓN EN TIEMPO REAL -----");
        System.out.println("Carros activos         : " + carrosActivos);
        System.out.println("Motos activas          : " + motosActivas);

        System.out.println("\n----- FLUJO HISTÓRICO ACUMULADO -----");
        System.out.println("Carros procesados      : " + carrosHistoricos);
        System.out.println("Motos procesadas       : " + motosHistoricas);

        System.out.println("\n----- TOTAL RECAUDADO -----");
        System.out.println("Ingresos globales      : " + formatoMoneda(ingresosTotales));
    }

    // ==========================================
    // BUSCAR FILTRO POR PLACA EN HISTORIAL
    // ==========================================
    public void buscarHistorialPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) return;
        String placaBusqueda = placa.toUpperCase().trim();

        long coincidencias = historial.stream()
                .filter(m -> m.getPlaca().equalsIgnoreCase(placaBusqueda))
                .peek(Movimiento::mostrarInformacion)
                .count();

        if (coincidencias == 0) {
            System.out.println("❌ No se encontraron registros de salidas históricos para la placa: " + placaBusqueda);
        }
    }

    // ==========================================
    // REPORTES FINANCIEROS AVANZADOS
    // ==========================================
    public void mostrarReporteFinanciero() {
        double ingresosHoy = 0;
        double ingresosMes = 0;
        double ingresosTotales = 0;

        java.time.LocalDate hoy = java.time.LocalDate.now();

        for (Movimiento m : historial) {
            double totalMovimiento = m.getTotal();
            ingresosTotales += totalMovimiento;

            if (m.getSalida().toLocalDate().equals(hoy)) {
                ingresosHoy += totalMovimiento;
            }

            if (m.getSalida().getYear() == hoy.getYear() && m.getSalida().getMonthValue() == hoy.getMonthValue()) {
                ingresosMes += totalMovimiento;
            }
        }

        double ticketPromedio = historial.isEmpty() ? 0 : ingresosTotales / historial.size();

        System.out.println("\n===== REPORTE FINANCIERO DE CAJA =====");
        System.out.println("Ingresos hoy      : " + formatoMoneda(ingresosHoy));
        System.out.println("Ingresos este mes : " + formatoMoneda(ingresosMes));
        System.out.println("Ingresos totales  : " + formatoMoneda(ingresosTotales));
        System.out.println("Ticket promedio   : " + formatoMoneda(ticketPromedio));
        System.out.println("Total transacciones: " + historial.size());
    }

    // =======================================================
    // RANKING VEHÍCULOS FRECUENTES (TOP 10 DASHBOARD)
    // =======================================================
    public void mostrarRankingVehiculos() {
        if (historial.isEmpty()) {
            System.out.println("\n❌ No existen movimientos registrados para generar el ranking.");
            return;
        }

        System.out.println("\n=======================================================");
        System.out.println("📊        DASHBOARD: TOP 10 VEHÍCULOS FRECUENTES       ");
        System.out.println("=======================================================");
        System.out.printf("%-6s | %-10s | %-12s | %-15s%n", "PUESTO", "PLACA", "VISITAS", "FRECUENCIA VISUAL");
        System.out.println("-------------------------------------------------------");

        java.util.Map<String, Integer> frecuencias = new java.util.HashMap<>();
        for (Movimiento m : historial) {
            frecuencias.put(m.getPlaca(), frecuencias.getOrDefault(m.getPlaca(), 0) + 1);
        }

        int maxVisitas = frecuencias.values().stream().max(Integer::compare).orElse(1);
        final int[] posicion = {1};
        
        frecuencias.entrySet().stream()
                .sorted((a, b) -> {
                    int comparaFrecuencia = b.getValue().compareTo(a.getValue());
                    if (comparaFrecuencia == 0) {
                        return a.getKey().compareTo(b.getKey()); // Desempate por placa
                    }
                    return comparaFrecuencia;
                })
                .limit(10)
                .forEach(item -> {
                    String placa = item.getKey();
                    int visitas = item.getValue();
                    
                    int tamañoBarra = (int) Math.round(((double) visitas / maxVisitas) * 10);
                    tamañoBarra = Math.max(1, tamañoBarra);
                    String barraVisual = "■".repeat(tamañoBarra);

                    System.out.printf("  #%02d  | %-10s | %-12s | %-15s%n", 
                            posicion[0]++, placa, visitas + " disp.", barraVisual);
                });

        System.out.println("=======================================================");
    }

    // =========================
    // ENLACE DE TRANSACCIONES
    // =========================
    private void guardarDatos() {
        FileUtil.guardarVehiculos(vehiculos);
        FileUtil.guardarHistorial(historial);
    }

    // =========================
    // FORMATO MONEDA CENTRAL
    // =========================
    private String formatoMoneda(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }
}