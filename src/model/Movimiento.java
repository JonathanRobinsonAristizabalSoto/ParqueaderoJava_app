// Ruta: src/model/Movimiento.java

package model;

import java.time.LocalDateTime;

/**
 * Registro de salida de vehículo
 */
public class Movimiento {

    private String placa;
    private TipoVehiculo tipo;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private double total;

    public Movimiento(
            String placa,
            TipoVehiculo tipo,
            LocalDateTime entrada,
            LocalDateTime salida,
            double total
    ) {
        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.entrada = entrada;
        this.salida = salida;
        this.total = total;
    }

    // =========================
    // GETTERS
    // =========================
    public String getPlaca() { return placa; }
    public TipoVehiculo getTipo() { return tipo; }
    public LocalDateTime getEntrada() { return entrada; }
    public LocalDateTime getSalida() { return salida; }
    public double getTotal() { return total; }

    // =========================
    // SERIALIZACIÓN
    // =========================
    public String toFile() {
        return placa + ";" + tipo + ";" + entrada + ";" + salida + ";" + total;
    }

    // =========================
    // DESERIALIZACIÓN
    // =========================
    public static Movimiento fromFile(String linea) {

        String[] d = linea.split(";");

        return new Movimiento(
                d[0],
                TipoVehiculo.valueOf(d[1]),
                LocalDateTime.parse(d[2]),
                LocalDateTime.parse(d[3]),
                Double.parseDouble(d[4])
        );
    }

    // =========================
    // PRINT
    // =========================
    public void mostrarInformacion() {

        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo);
        System.out.println("Entrada : " + entrada);
        System.out.println("Salida  : " + salida);
        System.out.println("Total   : $" + total);
    }
}