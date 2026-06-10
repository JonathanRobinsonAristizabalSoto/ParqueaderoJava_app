// Ruta: src/util/AppConfig.java

package util;

import java.io.File;

/**
 * Configuración centralizada del sistema.
 * Centraliza la gestión de rutas de persistencia para garantizar portabilidad multiplataforma.
 */
public class AppConfig {

    // Constructor privado para evitar instanciación accidental
    private AppConfig() {
        throw new UnsupportedOperationException("Esta es una clase de configuración estática y no puede ser instanciada.");
    }

    // ===================================
    // DIRECTORIO RAÍZ DE DATOS
    // ===================================
    private static final String BASE_PATH = System.getProperty("user.dir") + File.separator + "data";

    // ===================================
    // ARCHIVOS DE PERSISTENCIA CONSTANTES
    // ===================================
    public static final String VEHICULOS_FILE = BASE_PATH + File.separator + "vehiculos.txt";
    
    public static final String HISTORIAL_FILE = BASE_PATH + File.separator + "historial.txt";
    
    public static final String TARIFAS_FILE = BASE_PATH + File.separator + "tarifas.txt";

    // ===================================
    // MÉTODOS DE ACCESO
    // ===================================
    public static String getBasePath() {
        return BASE_PATH;
    }
}