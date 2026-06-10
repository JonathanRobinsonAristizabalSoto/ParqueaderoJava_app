// Ruta: src/util/Tarifas.java

package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manejo de tarifas del parqueadero con soporte de persistencia bidireccional
 */
public class Tarifas {

    private static double tarifaCarro = 4000;
    private static double tarifaMoto = 2000;

    private static final String FILE_PATH = AppConfig.getBasePath() + "/tarifas.txt";

    /**
     * Carga las tarifas guardadas desde el archivo persistente.
     * Si no existe, genera el archivo con los valores por defecto.
     */
    public static void cargarTarifas() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            crearArchivoDefault();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split("=");

                if (datos.length != 2) {
                    continue;
                }

                String clave = datos[0].trim();
                
                try {
                    double valor = Double.parseDouble(datos[1].trim());

                    switch (clave.toUpperCase()) {
                        case "CARRO" -> tarifaCarro = valor;
                        case "MOTO"  -> tarifaMoto = valor;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("⚠️ Formato numérico corrupto en tarifas.txt para: " + clave + ". Se usará el valor actual.");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error cargando tarifas: " + e.getMessage());
        }
    }

    /**
     * Escribe en caliente las tarifas actuales de la memoria en el archivo de texto.
     */
    public static void guardarTarifas() {
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            fw.write("CARRO=" + (long) tarifaCarro + "\n");
            fw.write("MOTO=" + (long) tarifaMoto + "\n");
        } catch (IOException e) {
            System.out.println("❌ Error guardando los cambios en tarifas.txt: " + e.getMessage());
        }
    }

    /**
     * Crea el archivo por defecto si es la primera ejecución.
     */
    private static void crearArchivoDefault() {
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            fw.write("CARRO=4000\n");
            fw.write("MOTO=2000\n");
        } catch (IOException e) {
            System.out.println("❌ Error creando tarifas.txt por defecto");
        }
    }

    // ===================================
    // GETTERS & SETTERS (Para el submenú)
    // ===================================
    public static double getTarifaCarro() {
        return tarifaCarro;
    }

    public static void setTarifaCarro(double nuevaTarifa) {
        tarifaCarro = nuevaTarifa;
    }

    public static double getTarifaMoto() {
        return tarifaMoto;
    }

    public static void setTarifaMoto(double nuevaTarifa) {
        tarifaMoto = nuevaTarifa;
    }
}