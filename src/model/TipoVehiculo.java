package model;

/**
 * Tipos de vehículo soportados por el sistema de parqueadero con mapeo estricto.
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
     * Retorna la representación visual o legible del tipo de vehículo.
     */
    public String getNombreLegible() {
        return nombreLegible;
    }

    /**
     * Valida de forma segura si una cadena de texto corresponde a un tipo soportado
     * sin lanzar excepciones, optimizando el rendimiento del backend.
     */
    public static boolean esValido(String value) {
        if (value == null || value.isBlank()) return false;
        String normalizado = value.trim().toUpperCase();
        for (TipoVehiculo tv : TipoVehiculo.values()) {
            if (tv.name().equals(normalizado)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte texto variable a un TipoVehiculo válido.
     * Debe llamarse únicamente después de haber validado con esValido().
     */
    public static TipoVehiculo fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El tipo de vehículo no puede estar vacío.");
        }

        return switch (value.trim().toUpperCase()) {
            case "CARRO" -> CARRO;
            case "MOTO"  -> MOTO;
            default      -> throw new IllegalArgumentException("Tipo de vehículo inválido: '" + value + "'");
        };
    }

    /**
     * Sobreescritura del método toString para mejorar la presentación en la interfaz.
     */
    @Override
    public String toString() {
        return this.nombreLegible;
    }
}