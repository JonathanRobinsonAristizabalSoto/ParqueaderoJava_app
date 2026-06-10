// Ruta: src/model/TipoVehiculo.java

package model;

/**
 * Tipos de vehículo soportados por el sistema.
 * Uso de ENUM para evitar errores de texto libre.
 */
public enum TipoVehiculo {
    CARRO,
    MOTO;

    /**
     * Convierte un texto a enum de forma segura.
     */
    public static TipoVehiculo fromString(String value) {

        if (value == null) return CARRO;

        return switch (value.trim().toLowerCase()) {
            case "moto" -> MOTO;
            default -> CARRO;
        };
    }
}