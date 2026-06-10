// Ruta: src/service/Parqueadero.java

package service;

import model.Movimiento;
import model.TipoVehiculo;
import model.Vehiculo;
import util.FileUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Lógica del sistema de parqueadero
 */
public class Parqueadero {

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

        for (Vehiculo v : vehiculos) {

            if (v.getPlaca().equalsIgnoreCase(placa)) {
                System.out.println("❌ Ya existe el vehículo");
                return;
            }
        }

        Vehiculo nuevo = new Vehiculo(
                placa,
                TipoVehiculo.fromString(tipo)
        );

        vehiculos.add(nuevo);

        FileUtil.guardarVehiculos(vehiculos);

        System.out.println("✅ Vehículo registrado");
    }

    // =========================
    // LISTAR
    // =========================
    public void mostrarVehiculos() {

        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos");
            return;
        }

        vehiculos.forEach(Vehiculo::mostrarInformacion);
    }

    // =========================
    // RETIRAR
    // =========================
    public void retirarVehiculo(String placa) {

        Vehiculo v = buscar(placa);

        if (v == null) {
            System.out.println("❌ No encontrado");
            return;
        }

        LocalDateTime salida = LocalDateTime.now();

        long horas = Math.max(
                1,
                Duration.between(
                        v.getHoraEntrada(),
                        salida
                ).toHours()
        );

        double tarifa =
                v.getTipo() == TipoVehiculo.MOTO
                        ? 2000
                        : 4000;

        double total = horas * tarifa;

        System.out.println("\n===== FACTURA =====");
        System.out.println("Placa : " + v.getPlaca());
        System.out.println("Tipo  : " + v.getTipo());
        System.out.println("Horas : " + horas);
        System.out.println("Total : $" + total);

        Movimiento movimiento = new Movimiento(
                v.getPlaca(),
                v.getTipo(),
                v.getHoraEntrada(),
                salida,
                total
        );

        historial.add(movimiento);

        vehiculos.remove(v);

        FileUtil.guardarVehiculos(vehiculos);
        FileUtil.guardarHistorial(historial);
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
    // BUSCAR
    // =========================
    private Vehiculo buscar(String placa) {

        return vehiculos.stream()
                .filter(v ->
                        v.getPlaca()
                                .equalsIgnoreCase(placa))
                .findFirst()
                .orElse(null);
    }
}