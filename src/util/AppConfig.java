package util;

/**
 * Configuración centralizada de los parámetros del sistema y la base de datos.
 */
public class AppConfig {

    private AppConfig() {
        throw new UnsupportedOperationException("Clase de utilidad estática.");
    }

    // ===================================
    // CONFIGURACIÓN DE CONEXIÓN MYSQL
    // ===================================
    // Asegúrate de que estos valores coincidan con tu configuración en XAMPP
    public static final String DB_URL = "jdbc:mysql://localhost:3306/sistema_parqueadero";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Por defecto en XAMPP suele ser vacío

    // ===================================
    // PARÁMETROS OPERATIVOS
    // ===================================
    public static final int CAPACIDAD_MAXIMA = 20;
}