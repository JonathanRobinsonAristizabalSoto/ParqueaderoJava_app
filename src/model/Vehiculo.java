// Ruta: src/model/Vehiculo.java

package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidad Vehículo
 */
public class Vehiculo {

    // =========================
    // FORMATO FECHA
    // =========================
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================
    // ATRIBUTOS
    // =========================
    private String placa;
    private TipoVehiculo tipo;
    private LocalDateTime horaEntrada;

    // =========================
    // CONSTRUCTOR NUEVO
    // =========================
    public Vehiculo(String placa, TipoVehiculo tipo) {

        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.horaEntrada = LocalDateTime.now();
    }

    // =========================
    // CONSTRUCTOR DESDE ARCHIVO
    // =========================
    public Vehiculo(
            String placa,
            TipoVehiculo tipo,
            LocalDateTime horaEntrada
    ) {

        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.horaEntrada = horaEntrada;
    }

    // =========================
    // GETTERS
    // =========================
    public String getPlaca() {
        return placa;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    // =========================
    // SERIALIZACIÓN
    // =========================
    public String toFile() {

        return placa
                + ";"
                + tipo
                + ";"
                + horaEntrada;
    }

    // =========================
    // DESERIALIZACIÓN
    // =========================
    public static Vehiculo fromFile(String linea) {

        String[] d = linea.split(";");

        return new Vehiculo(
                d[0],
                TipoVehiculo.valueOf(d[1]),
                LocalDateTime.parse(d[2])
        );
    }

    // =========================
    // PRESENTACIÓN
    // =========================
    public void mostrarInformacion() {

        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo);
        System.out.println(
                "Entrada : "
                        + horaEntrada.format(FORMATO_FECHA)
        );
    }
}