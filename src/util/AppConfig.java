package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    public static final String DB_URL = "jdbc:mysql://localhost:3306/sistema_parqueadero";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; 

    // ===================================
    // PARÁMETROS OPERATIVOS DINÁMICOS
    // ===================================
    
    public static int obtenerCapacidadMaxima() {
        String sql = "SELECT valor FROM configuracion WHERE clave = 'capacidad_maxima'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString("valor"));
            }
        } catch (SQLException | NumberFormatException e) {
            // Log sutil o fallback por si la DB se está inicializando
        }
        return 20; // Retorno seguro por defecto si falla la consulta
    }

    public static void actualizarCapacidadMaxima(int nuevaCapacidad) {
        if (nuevaCapacidad <= 0) {
            System.out.println("❌ Error: La capacidad debe ser mayor a 0.");
            return;
        }
        String sql = "INSERT INTO configuracion (clave, valor) VALUES ('capacidad_maxima', ?) " +
                     "ON DUPLICATE KEY UPDATE valor = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(nuevaCapacidad));
            ps.setString(2, String.valueOf(nuevaCapacidad));
            ps.executeUpdate();
            System.out.println("✅ Capacidad máxima del parqueadero actualizada a: " + nuevaCapacidad);
        } catch (SQLException e) {
            System.out.println("❌ Error al cambiar la capacidad en la DB: " + e.getMessage());
        }
    }
}