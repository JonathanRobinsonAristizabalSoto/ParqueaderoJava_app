package service;

import model.TipoVehiculo;
import util.AppConfig;
import util.ConexionDB;
import java.sql.*;

public class ManejadorConfiguracion {

    // ==========================================
    // GESTIÓN DE CAPACIDAD MÁXIMA
    // ==========================================
    public int obtenerCapacidadMaxima() {
        // Delegamos directamente a la utilidad centralizada para evitar código espejo
        return AppConfig.obtenerCapacidadMaxima();
    }

    public void actualizarCapacidadMaxima(int nuevaCapacidad) {
        AppConfig.actualizarCapacidadMaxima(nuevaCapacidad);
    }

    // ==========================================
    // GESTIÓN DE TARIFAS
    // ==========================================
    public void actualizarTarifa(TipoVehiculo tipo, double nuevoValor) {
        if (tipo == null) return;
        actualizarTarifa(tipo.name(), nuevoValor);
    }

    // Sobrecarga compatible con la firma original que usaba el Main
    public void actualizarTarifa(String tipoId, double nuevoValor) {
        if (nuevoValor < 0) {
            System.out.println("❌ Error: El valor de la tarifa no puede ser negativo.");
            return;
        }
        String sql = "UPDATE tarifas SET valor_hora = ? WHERE tipo_vehiculo = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDouble(1, nuevoValor);
            ps.setString(2, tipoId.toUpperCase().trim());
            
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("✅ Tarifa de " + tipoId.toUpperCase() + " actualizada con éxito a: $" + String.format("%,.0f", nuevoValor));
            } else {
                System.out.println("❌ No se encontró la tarifa para el tipo '" + tipoId + "' en la base de datos.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar la tarifa: " + e.getMessage());
        }
    }
}