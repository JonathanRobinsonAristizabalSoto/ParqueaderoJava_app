// Ruta: src/model/TipoVehiculo.java

package model;

/**
 * Tipos de vehículo soportados por el sistema.
 */
public enum TipoVehiculo {

    CARRO,
    MOTO;

    /**
     * Convierte texto a TipoVehiculo.
     */
    public static TipoVehiculo fromString(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tipo de vehículo vacío");
        }

        return switch (value.trim().toUpperCase()) {

            case "CARRO" -> CARRO;
            case "MOTO" -> MOTO;

            default ->
                throw new IllegalArgumentException(
                    "Tipo de vehículo inválido: " + value
                );
        };
    }
}