// Ruta: src/util/FileUtil.java

package util;

import model.Movimiento;
import model.Vehiculo;

import java.io.*;
import java.util.ArrayList;

/**
 * Utilidades para manejo de archivos
 */
public class FileUtil {

    /**
     * Garantiza que exista la carpeta data
     */
    public static void ensureDataFolder() {

        File folder = new File(AppConfig.getBasePath());

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Guarda vehículos activos
     */
    public static void guardarVehiculos(ArrayList<Vehiculo> vehiculos) {

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(AppConfig.VEHICULOS_FILE))) {

            for (Vehiculo v : vehiculos) {
                bw.write(v.toFile());
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("❌ Error guardando vehículos: " + e.getMessage());
        }
    }

    /**
     * Carga vehículos desde archivo
     */
    public static ArrayList<Vehiculo> cargarVehiculos() {

        ArrayList<Vehiculo> vehiculos = new ArrayList<>();

        File file = new File(AppConfig.VEHICULOS_FILE);

        if (!file.exists()) {
            return vehiculos;
        }

        try (BufferedReader br = new BufferedReader(
                new FileReader(file))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                vehiculos.add(Vehiculo.fromFile(linea));
            }

        } catch (Exception e) {
            System.out.println("❌ Error cargando vehículos: " + e.getMessage());
        }

        return vehiculos;
    }

    /**
     * Guarda historial
     */
    public static void guardarHistorial(ArrayList<Movimiento> historial) {

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(AppConfig.HISTORIAL_FILE))) {

            for (Movimiento m : historial) {
                bw.write(m.toFile());
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("❌ Error guardando historial: " + e.getMessage());
        }
    }

    /**
     * Carga historial
     */
    public static ArrayList<Movimiento> cargarHistorial() {

        ArrayList<Movimiento> historial = new ArrayList<>();

        File file = new File(AppConfig.HISTORIAL_FILE);

        if (!file.exists()) {
            return historial;
        }

        try (BufferedReader br = new BufferedReader(
                new FileReader(file))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                historial.add(Movimiento.fromFile(linea));
            }

        } catch (Exception e) {
            System.out.println("❌ Error cargando historial: " + e.getMessage());
        }

        return historial;
    }
}