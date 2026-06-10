// Ruta: src/model/Movimiento.java

package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un movimiento histórico
 * de entrada y salida de un vehículo.
 */
public class Movimiento {

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
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private double total;

    // =========================
    // CONSTRUCTOR
    // =========================
    public Movimiento(
            String placa,
            TipoVehiculo tipo,
            LocalDateTime entrada,
            LocalDateTime salida,
            double total) {

        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.entrada = entrada;
        this.salida = salida;
        this.total = total;
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

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public double getTotal() {
        return total;
    }

    // =========================
    // SERIALIZACIÓN
    // =========================
    public String toFile() {

        return placa + ";"
                + tipo + ";"
                + entrada + ";"
                + salida + ";"
                + total;
    }

    // =========================
    // DESERIALIZACIÓN
    // =========================
    public static Movimiento fromFile(String linea) {

        String[] d = linea.split(";");

        if (d.length != 5) {
            throw new IllegalArgumentException(
                    "Formato inválido: " + linea
            );
        }

        return new Movimiento(
                d[0],
                TipoVehiculo.fromString(d[1]),
                LocalDateTime.parse(d[2]),
                LocalDateTime.parse(d[3]),
                Double.parseDouble(d[4])
        );
    }

    // =========================
    // FORMATO DINERO
    // =========================
    private String formatearDinero(double valor) {

        return String.format("%,.0f", valor)
                .replace(",", ".");
    }

    // =========================
    // TIEMPO DE PERMANENCIA
    // =========================
    private String calcularPermanencia() {

        Duration duracion =
                Duration.between(entrada, salida);

        long horas = duracion.toHours();

        long minutos =
                duracion.toMinutes() % 60;

        return horas
                + " hora(s) "
                + minutos
                + " minuto(s)";
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
                        + entrada.format(FORMATO_FECHA)
        );

        System.out.println(
                "Salida  : "
                        + salida.format(FORMATO_FECHA)
        );

        System.out.println(
                "Tiempo  : "
                        + calcularPermanencia()
        );

        System.out.println(
                "Total   : $"
                        + formatearDinero(total)
        );
    }
}