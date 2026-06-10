package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un registro de movimiento histórico.
 * La persistencia ahora es manejada por MySQL.
 */
public class Movimiento {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String placa;
    private final TipoVehiculo tipo;
    private final LocalDateTime entrada;
    private final LocalDateTime salida;
    private final double total;

    public Movimiento(String placa, TipoVehiculo tipo, LocalDateTime entrada, LocalDateTime salida, double total) {
        this.placa = placa.toUpperCase().trim();
        this.tipo = tipo;
        this.entrada = entrada;
        this.salida = salida;
        this.total = total;
    }

    // Getters para acceder a los datos después de un ResultSet
    public String getPlaca() { return placa; }
    public TipoVehiculo getTipo() { return tipo; }
    public LocalDateTime getEntrada() { return entrada; }
    public LocalDateTime getSalida() { return salida; }
    public double getTotal() { return total; }

    // Lógica interna de presentación
    private String calcularPermanencia() {
        Duration duracion = Duration.between(entrada, salida);
        long horas = duracion.toHours();
        long minutos = duracion.toMinutes() % 60;
        return horas + " hora(s) " + minutos + " minuto(s)";
    }

    private String formatearDinero(double valor) {
        return String.format("%,.0f", valor).replace(",", ".");
    }

    public void mostrarInformacion() {
        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo);
        System.out.println("Entrada : " + entrada.format(FORMATO_FECHA));
        System.out.println("Salida  : " + salida.format(FORMATO_FECHA));
        System.out.println("Tiempo  : " + calcularPermanencia());
        System.out.println("Total   : $" + formatearDinero(total));
    }
}