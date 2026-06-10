// Ruta: src/service/Parqueadero.java

package service;

import model.*;
import util.AppConfig;
import util.FileUtil;

import java.io.*;
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

    public Parqueadero() {
        FileUtil.ensureDataFolder();
        cargarVehiculos();
        cargarHistorial();
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

        vehiculos.add(new Vehiculo(placa, TipoVehiculo.fromString(tipo)));
        guardarVehiculos();

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
    // SALIDA
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
                Duration.between(v.getHoraEntrada(), salida).toHours()
        );

        double tarifa = v.getTipo() == TipoVehiculo.MOTO ? 2000 : 4000;
        double total = horas * tarifa;

        System.out.println("\n===== FACTURA =====");
        System.out.println("Placa: " + v.getPlaca());
        System.out.println("Total: $" + total);

        historial.add(new Movimiento(
                v.getPlaca(),
                v.getTipo(),
                v.getHoraEntrada(),
                salida,
                total
        ));

        vehiculos.remove(v);

        guardarVehiculos();
        guardarHistorial();
    }

    // =========================
    // HISTORIAL
    // =========================
    public void mostrarHistorial() {
        historial.forEach(Movimiento::mostrarInformacion);
    }

    // =========================
    // BUSCAR
    // =========================
    private Vehiculo buscar(String placa) {

        return vehiculos.stream()
                .filter(v -> v.getPlaca().equalsIgnoreCase(placa))
                .findFirst()
                .orElse(null);
    }

    // =========================
    // ARCHIVOS
    // =========================
    private void guardarVehiculos() {

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(AppConfig.VEHICULOS_FILE))) {

            for (Vehiculo v : vehiculos) {
                bw.write(v.toFile());
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error vehiculos: " + e.getMessage());
        }
    }

    private void guardarHistorial() {

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(AppConfig.HISTORIAL_FILE))) {

            for (Movimiento m : historial) {
                bw.write(m.toFile());
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error historial: " + e.getMessage());
        }
    }

    private void cargarVehiculos() {

        File file = new File(AppConfig.VEHICULOS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                vehiculos.add(Vehiculo.fromFile(line));
            }

        } catch (Exception e) {
            System.out.println("Error load vehiculos");
        }
    }

    private void cargarHistorial() {

        File file = new File(AppConfig.HISTORIAL_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                historial.add(Movimiento.fromFile(line));
            }

        } catch (Exception e) {
            System.out.println("Error load historial");
        }
    }
}