package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para gestionar la conexión a la base de datos MySQL.
 * Utiliza las credenciales definidas en AppConfig.
 */
public class ConexionDB {

    // Bloque estático para cargar el Driver una sola vez al cargar la clase en memoria
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: No se encontró el driver de MySQL (Connector/J). " +
                               "Asegúrate de tener la librería en tu classpath.");
        }
    }

    /**
     * Establece y retorna una nueva conexión con la base de datos.
     * @return Connection objeto de conexión SQL
     * @throws SQLException si las credenciales fallan o la BD no responde
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Se recomienda usar el parámetro useSSL=false o true según tu configuración de servidor
            String url = AppConfig.DB_URL;
            String user = AppConfig.DB_USER;
            String password = AppConfig.DB_PASSWORD;

            return DriverManager.getConnection(url, user, password);
            
        } catch (SQLException e) {
            System.err.println("❌ Error crítico: Fallo al conectar a la base de datos.");
            System.err.println("   Detalle: " + e.getMessage());
            throw e; // Lanzamos la excepción para que el servicio que llama pueda manejarla
        }
    }
}