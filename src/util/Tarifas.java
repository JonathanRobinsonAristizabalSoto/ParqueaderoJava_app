// Ruta: src/util/Tarifas.java

package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manejo de tarifas del parqueadero
 */
public class Tarifas {

    private static double tarifaCarro = 4000;
    private static double tarifaMoto = 2000;

    private static final String FILE_PATH =
            AppConfig.getBasePath() + "/tarifas.txt";

    /**
     * Carga tarifas desde archivo
     */
    public static void cargarTarifas() {

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            crearArchivoDefault();
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(file))) {

            String linea;

            while ((linea = br.readLine()) != null) {

                String[] datos = linea.split("=");

                if (datos.length != 2) {
                    continue;
                }

                String clave = datos[0].trim();
                double valor = Double.parseDouble(datos[1].trim());

                switch (clave.toUpperCase()) {

                    case "CARRO" -> tarifaCarro = valor;

                    case "MOTO" -> tarifaMoto = valor;
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error cargando tarifas: "
                            + e.getMessage());
        }
    }

    /**
     * Crear archivo por defecto
     */
    private static void crearArchivoDefault() {

        try (FileWriter fw =
                     new FileWriter(FILE_PATH)) {

            fw.write("CARRO=4000\n");
            fw.write("MOTO=2000\n");

        } catch (IOException e) {

            System.out.println(
                    "Error creando tarifas.txt");
        }
    }

    public static double getTarifaCarro() {
        return tarifaCarro;
    }

    public static double getTarifaMoto() {
        return tarifaMoto;
    }
}