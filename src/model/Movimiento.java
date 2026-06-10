// Ruta: src/model/Movimiento.java

package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un movimiento histórico de entrada y salida de un vehículo.
 */
public class Movimiento {

    // =========================
    // FORMATO FECHA GLOBAL (UI)
    // =========================
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================
    // ATRIBUTOS (Inmutables)
    // =========================
    private final String placa;
    private final TipoVehiculo tipo;
    private final LocalDateTime entrada;
    private final LocalDateTime salida;
    private final double total;

    // =========================
    // CONSTRUCTOR
    // =========================
    public Movimiento(String placa, TipoVehiculo tipo, LocalDateTime entrada, LocalDateTime salida, double total) {
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
    // SERIALIZACIÓN (.txt)
    // =========================
    public String toFile() {
        // Guardamos explícitamente el nombre de la constante del enum (CARRO/MOTO)
        return placa + ";" + tipo.name() + ";" + entrada + ";" + salida + ";" + total;
    }

    // =========================
    // DESERIALIZACIÓN (.txt)
    // =========================
    public static Movimiento fromFile(String linea) {
        if (linea == null || linea.isBlank()) {
            return null;
        }

        try {
            String[] d = linea.split(";");

            if (d.length < 5) {
                return null; // Registro incompleto en disco
            }

            String placaRecuperada = d[0].toUpperCase().trim();
            TipoVehiculo tipoRecuperado = TipoVehiculo.valueOf(d[1].toUpperCase().trim());
            LocalDateTime entradaRecuperada = LocalDateTime.parse(d[2].trim());
            LocalDateTime salidaRecuperada = LocalDateTime.parse(d[3].trim());
            double totalRecuperado = Double.parseDouble(d[4].trim());

            return new Movimiento(placaRecuperada, tipoRecuperado, entradaRecuperada, salidaRecuperada, totalRecuperado);

        } catch (Exception e) {
            // Evita que un registro corrupto detenga la carga total del programa
            System.out.println("⚠️ Error al deserializar movimiento histórico en línea: [" + linea + "]. Saltando registro.");
            return null;
        }
    }

    // =========================
    // FORMATO DINERO
    // =========================
    private String formatearDinero(double valor) {
        return String.format("%,.0f", valor).replace(",", ".");
    }

    // =========================
    // TIEMPO DE PERMANENCIA
    // =========================
    private String calcularPermanencia() {
        Duration duracion = Duration.between(entrada, salida);
        long horas = duracion.toHours();
        long minutos = duracion.toMinutes() % 60;

        return horas + " hora(s) " + minutos + " minuto(s)";
    }

    // =========================
    // PRESENTACIÓN
    // =========================
    public void mostrarInformacion() {
        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo); // Usará el toString() limpio ("Carro"/"Moto")
        System.out.println("Entrada : " + entrada.format(FORMATO_FECHA));
        System.out.println("Salida  : " + salida.format(FORMATO_FECHA));
        System.out.println("Tiempo  : " + calcularPermanencia());
        System.out.println("Total   : $" + formatearDinero(total));
    }
}