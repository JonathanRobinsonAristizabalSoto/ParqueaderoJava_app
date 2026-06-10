package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    
    /**
     * Establece una conexión con MySQL utilizando las credenciales 
     * definidas centralizadamente en AppConfig.
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Carga explícita del driver (opcional en versiones recientes, pero recomendado)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            return DriverManager.getConnection(
                AppConfig.DB_URL, 
                AppConfig.DB_USER, 
                AppConfig.DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL (Connector/J). Asegúrate de tenerlo en tu carpeta /lib", e);
        }
    }
}