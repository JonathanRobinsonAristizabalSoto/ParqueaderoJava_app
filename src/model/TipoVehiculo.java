// Ruta: src/model/TipoVehiculo.java

package model;

/**
 * Tipos de vehículo soportados por el sistema de parqueadero.
 */
public enum TipoVehiculo {
    
    CARRO("Carro"),
    MOTO("Moto");

    private final String nombreLegible;

    // Constructor del enum para asignar la representación visual
    TipoVehiculo(String nombreLegible) {
        this.nombreLegible = nombreLegible;
    }

    /**
     * Convierte texto variable proveniente de la consola a un TipoVehiculo válido.
     * Soporta espacios adicionales y variaciones de mayúsculas/minúsculas.
     */
    public static TipoVehiculo fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El tipo de vehículo no puede estar vacío.");
        }

        return switch (value.trim().toUpperCase()) {
            case "CARRO" -> CARRO;
            case "MOTO"  -> MOTO;
            default     -> throw new IllegalArgumentException("Tipo de vehículo inválido: '" + value + "'");
        };
    }

    /**
     * Sobreescritura del método toString para mejorar la presentación en la interfaz de consola.
     */
    @Override
    public String toString() {
        return this.nombreLegible;
    }
}