// Ruta: src/util/FileUtil.java

package util;

import model.Movimiento;
import model.Vehiculo;

import java.io.*;
import java.util.ArrayList;

/**
 * Utilidades para el manejo y persistencia de archivos de datos.
 */
public class FileUtil {

    // Constructor privado para evitar instanciación innecesaria
    private FileUtil() {
        throw new UnsupportedOperationException("Clase utilitaria estática.");
    }

    /**
     * Garantiza de forma preventiva que exista la carpeta contenedora de persistencia.
     */
    public static void ensureDataFolder() {
        File folder = new File(AppConfig.getBasePath());
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Guarda la lista de vehículos activos en el archivo persistente de texto.
     */
    public static void guardarVehiculos(ArrayList<Vehiculo> vehiculos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AppConfig.VEHICULOS_FILE))) {

            for (Vehiculo v : vehiculos) {
                if (v != null) {
                    bw.write(v.toFile());
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Error guardando vehículos en disco: " + e.getMessage());
        }
    }

    /**
     * Carga y reconstruye los vehículos activos desde el archivo de texto.
     * Filtra de forma automática líneas vacías o corruptas.
     */
    public static ArrayList<Vehiculo> cargarVehiculos() {
        ArrayList<Vehiculo> vehiculos = new ArrayList<>();
        File file = new File(AppConfig.VEHICULOS_FILE);

        if (!file.exists()) {
            return vehiculos;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                Vehiculo v = Vehiculo.fromFile(linea);
                // Control preventivo: Solo agregamos si la deserialización fue exitosa
                if (v != null) {
                    vehiculos.add(v);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error crítico leyendo archivo de vehículos: " + e.getMessage());
        }

        return vehiculos;
    }

    /**
     * Guarda la colección de movimientos históricos en el archivo correspondiente.
     */
    public static void guardarHistorial(ArrayList<Movimiento> historial) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AppConfig.HISTORIAL_FILE))) {

            for (Movimiento m : historial) {
                if (m != null) {
                    bw.write(m.toFile());
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Error guardando historial en disco: " + e.getMessage());
        }
    }

    /**
     * Carga y reconstruye los movimientos históricos desde el archivo de texto.
     * Filtra registros corruptos de manera transparente.
     */
    public static ArrayList<Movimiento> cargarHistorial() {
        ArrayList<Movimiento> historial = new ArrayList<>();
        File file = new File(AppConfig.HISTORIAL_FILE);

        if (!file.exists()) {
            return historial;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                Movimiento m = Movimiento.fromFile(linea);
                // Control preventivo: Evita inyectar nulos a la colección de memoria
                if (m != null) {
                    historial.add(m);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error crítico leyendo archivo de historial: " + e.getMessage());
        }

        return historial;
    }
}