// Ruta: src/util/AppConfig.java

package util;

import java.io.File;

/**
 * Configuración central del sistema
 * Evita rutas hardcodeadas en el código
 */
public class AppConfig {

    private static final String BASE_PATH =
            System.getProperty("user.dir") + File.separator + "data";

    public static final String VEHICULOS_FILE =
            BASE_PATH + File.separator + "vehiculos.txt";

    public static final String HISTORIAL_FILE =
            BASE_PATH + File.separator + "historial.txt";

    public static String getBasePath() {
        return BASE_PATH;
    }
}