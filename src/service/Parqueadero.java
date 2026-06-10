// Ruta: src/service/Parqueadero.java

package service;

import model.Movimiento;
import model.TipoVehiculo;
import model.Vehiculo;
import util.FileUtil;
import util.Tarifas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

/**
 * Lógica del sistema de parqueadero
 */
public class Parqueadero {

    // =========================
    // CONFIGURACIÓN
    // =========================
    private static final int CAPACIDAD_MAXIMA = 20;

    // =========================
    // MEMORIA
    // =========================
    private final ArrayList<Vehiculo> vehiculos = new ArrayList<>();
    private final ArrayList<Movimiento> historial = new ArrayList<>();

    // =========================
    // CONSTRUCTOR
    // =========================
    public Parqueadero() {

        FileUtil.ensureDataFolder();

        Tarifas.cargarTarifas();

        vehiculos.addAll(FileUtil.cargarVehiculos());
        historial.addAll(FileUtil.cargarHistorial());
    }

    // =========================
    // INGRESO
    // =========================
    public void ingresarVehiculo(String placa, String tipo) {

        if (placa == null || placa.isBlank()) {
            System.out.println("❌ Placa inválida");
            return;
        }

        if (vehiculos.size() >= CAPACIDAD_MAXIMA) {
            System.out.println("❌ Parqueadero lleno");
            return;
        }

        for (Vehiculo v : vehiculos) {

            if (v.getPlaca().equalsIgnoreCase(placa)) {
                System.out.println("❌ Ya existe el vehículo");
                return;
            }
        }

        Vehiculo nuevo = new Vehiculo(
                placa,
                TipoVehiculo.fromString(tipo));

        vehiculos.add(nuevo);

        FileUtil.guardarVehiculos(vehiculos);

        System.out.println("✅ Vehículo registrado");
        System.out.println("🚗 Espacios ocupados: " + vehiculos.size());
        System.out.println("🅿️ Espacios disponibles: " + getEspaciosDisponibles());
    }

    // =========================
    // LISTAR
    // =========================
    public void mostrarVehiculos() {

        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos");
            return;
        }

        System.out.println("\n===== VEHÍCULOS ACTIVOS =====");

        vehiculos.forEach(Vehiculo::mostrarInformacion);

        System.out.println("\nCapacidad total : " + CAPACIDAD_MAXIMA);
        System.out.println("Ocupados        : " + vehiculos.size());
        System.out.println("Disponibles     : " + getEspaciosDisponibles());
    }

    // =========================
    // RETIRAR
    // =========================
    public void retirarVehiculo(String placa) {

        Vehiculo vehiculo = buscar(placa);

        if (vehiculo == null) {
            System.out.println("❌ Vehículo no encontrado");
            return;
        }

        LocalDateTime salida = LocalDateTime.now();

        Duration duracion = Duration.between(
        vehiculo.getHoraEntrada(),
        salida);

long minutos = duracion.toMinutes();

long horasFacturadas = Math.max(
        1,
        (long) Math.ceil(minutos / 60.0)
);

        double tarifa = vehiculo.getTipo() == TipoVehiculo.MOTO
                ? Tarifas.getTarifaMoto()
                : Tarifas.getTarifaCarro();

        double total = horasFacturadas * tarifa;

        System.out.println("\n===== FACTURA =====");
        System.out.println("Placa   : " + vehiculo.getPlaca());
        System.out.println("Tipo    : " + vehiculo.getTipo());
        System.out.println("Entrada : " + vehiculo.getHoraEntrada().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Salida  : " + salida.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Horas   : " + horasFacturadas);
        System.out.println("Tarifa  : " + formatoMoneda(tarifa));
        System.out.println("Total   : " + formatoMoneda(total));

        Movimiento movimiento = new Movimiento(
                vehiculo.getPlaca(),
                vehiculo.getTipo(),
                vehiculo.getHoraEntrada(),
                salida,
                total);

        historial.add(movimiento);

        vehiculos.remove(vehiculo);

        guardarDatos();

        System.out.println("\n✅ Vehículo retirado");
        System.out.println("🚗 Espacios ocupados: " + vehiculos.size());
        System.out.println("🅿️ Espacios disponibles: " + getEspaciosDisponibles());
    }

    // =========================
    // HISTORIAL
    // =========================
    public void mostrarHistorial() {

        if (historial.isEmpty()) {
            System.out.println("No hay movimientos registrados");
            return;
        }

        historial.forEach(Movimiento::mostrarInformacion);
    }

    // =========================
    // BUSCAR VEHÍCULO
    // =========================
    public void buscarVehiculo(String placa) {

        Vehiculo vehiculo = buscar(placa);

        if (vehiculo == null) {
            System.out.println("❌ Vehículo no encontrado");
            return;
        }

        System.out.println("\n===== VEHÍCULO ENCONTRADO =====");

        vehiculo.mostrarInformacion();
    }

    // =========================
    // ESTADO PARQUEADERO
    // =========================
    public void mostrarEstado() {

        System.out.println("\n===== ESTADO DEL PARQUEADERO =====");
        System.out.println("Capacidad total : " + CAPACIDAD_MAXIMA);
        System.out.println("Ocupados        : " + vehiculos.size());
        System.out.println("Disponibles     : " + getEspaciosDisponibles());
    }

    // =========================
    // ESPACIOS DISPONIBLES
    // =========================
    private int getEspaciosDisponibles() {
        return CAPACIDAD_MAXIMA - vehiculos.size();
    }

    // =========================
    // BUSCAR
    // =========================
    private Vehiculo buscar(String placa) {

        return vehiculos.stream()
                .filter(v -> v.getPlaca()
                        .equalsIgnoreCase(placa))
                .findFirst()
                .orElse(null);
    }

    // =========================
    // ESTADÍSTICAS
    // =========================
    public void mostrarEstadisticas() {

        int carrosActivos = 0;
        int motosActivas = 0;

        for (Vehiculo v : vehiculos) {

            if (v.getTipo() == TipoVehiculo.CARRO) {
                carrosActivos++;
            } else {
                motosActivas++;
            }
        }

        int carrosHistoricos = 0;
        int motosHistoricas = 0;

        double ingresosTotales = 0;

        for (Movimiento m : historial) {

            if (m.getTipo() == TipoVehiculo.CARRO) {
                carrosHistoricos++;
            } else {
                motosHistoricas++;
            }

            ingresosTotales += m.getTotal();
        }

        System.out.println("\n===== ESTADÍSTICAS =====");

        System.out.println("Vehículos activos      : " + vehiculos.size());
        System.out.println("Movimientos históricos : " + historial.size());

        System.out.println("\n----- ACTIVOS -----");
        System.out.println("Carros activos         : " + carrosActivos);
        System.out.println("Motos activas          : " + motosActivas);

        System.out.println("\n----- HISTÓRICO -----");
        System.out.println("Carros históricos      : " + carrosHistoricos);
        System.out.println("Motos históricas       : " + motosHistoricas);

        System.out.println("\n----- INGRESOS -----");
        System.out.printf("Ingresos generados     : $%,.0f%n", ingresosTotales);
    }

    // =========================
    // BUSCAR HISTORIAL POR PLACA
    // =========================
    public void buscarHistorialPorPlaca(String placa) {

        boolean encontrado = false;

        System.out.println("\n===== HISTORIAL DE " + placa.toUpperCase() + " =====");

        for (Movimiento movimiento : historial) {

            if (movimiento.getPlaca().equalsIgnoreCase(placa)) {

                movimiento.mostrarInformacion();
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("❌ No existen movimientos para esa placa");
        }
    }

    // =========================
    // REPORTE FINANCIERO
    // =========================
    public void mostrarReporteFinanciero() {

        double ingresosHoy = 0;
        double ingresosMes = 0;
        double ingresosTotales = 0;

        java.time.LocalDate hoy = java.time.LocalDate.now();

        for (Movimiento movimiento : historial) {

            ingresosTotales += movimiento.getTotal();

            if (movimiento.getSalida().toLocalDate().equals(hoy)) {
                ingresosHoy += movimiento.getTotal();
            }

            if (movimiento.getSalida().getYear() == hoy.getYear()
                    && movimiento.getSalida().getMonthValue() == hoy.getMonthValue()) {

                ingresosMes += movimiento.getTotal();
            }
        }

        double ticketPromedio = historial.isEmpty()
                ? 0
                : ingresosTotales / historial.size();

        System.out.println("\n===== REPORTE FINANCIERO =====");

        System.out.printf(
                "Ingresos hoy      : $%,.0f%n",
                ingresosHoy);

        System.out.printf(
                "Ingresos este mes : $%,.0f%n",
                ingresosMes);

        System.out.printf(
                "Ingresos totales  : $%,.0f%n",
                ingresosTotales);

        System.out.printf(
                "Ticket promedio   : $%,.0f%n",
                ticketPromedio);

        System.out.println(
                "Movimientos       : "
                        + historial.size());
    }

    // =========================
    // RANKING VEHÍCULOS FRECUENTES
    // =========================
    public void mostrarRankingVehiculos() {

        if (historial.isEmpty()) {

            System.out.println("\n❌ No existen movimientos registrados");
            return;
        }

        java.util.HashMap<String, Integer> ranking = new java.util.HashMap<>();

        for (Movimiento movimiento : historial) {

            String placa = movimiento.getPlaca();

            ranking.put(
                    placa,
                    ranking.getOrDefault(placa, 0) + 1);
        }

        ArrayList<java.util.Map.Entry<String, Integer>> lista = new ArrayList<>(ranking.entrySet());

        lista.sort(
                (a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("\n===== VEHÍCULOS MÁS FRECUENTES =====");

        int posicion = 1;

        for (java.util.Map.Entry<String, Integer> item : lista) {

            System.out.println(
                    posicion +
                            ". " +
                            item.getKey() +
                            " -> " +
                            item.getValue() +
                            " visita(s)");

            posicion++;
        }
    }

    // =========================
    // GUARDAR DATOS
    // =========================
    private void guardarDatos() {

        FileUtil.guardarVehiculos(vehiculos);
        FileUtil.guardarHistorial(historial);
    }

    // =========================
    // FORMATO MONEDA
    // =========================
    private String formatoMoneda(double valor) {

        return "$"
                + String.format("%,.0f", valor)
                        .replace(",", ".");
    }
}