// Ruta: src/util/FileUtil.java

package util;

import java.io.File;

/**
 * Utilidades de archivos
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
}