package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidad Vehículo
 * Adaptada para arquitectura MySQL.
 */
public class Vehiculo {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String placa;
    private final TipoVehiculo tipo;
    private final LocalDateTime horaEntrada;

    // Este es el único constructor necesario para el registro de nuevos vehículos
    public Vehiculo(String placa, TipoVehiculo tipo, LocalDateTime horaEntrada) {
        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.horaEntrada = horaEntrada;
    }

    // Getters
    public String getPlaca() { return placa; }
    public TipoVehiculo getTipo() { return tipo; }
    public LocalDateTime getHoraEntrada() { return horaEntrada; }

    // Método de presentación mejorado
    public void mostrarInformacion() {
        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo);
        System.out.println("Entrada : " + horaEntrada.format(FORMATO_FECHA));
    }
}