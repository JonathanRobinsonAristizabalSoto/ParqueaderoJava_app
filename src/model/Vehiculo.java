// Ruta: src/model/Vehiculo.java

package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidad Vehículo
 */
public class Vehiculo {

    // =========================
    // FORMATO FECHA GLOBAL (UI)
    // =========================
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================
    // ATRIBUTOS
    // =========================
    private final String placa;
    private final TipoVehiculo tipo;
    private final LocalDateTime horaEntrada;

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
    public Vehiculo(String placa, TipoVehiculo tipo, LocalDateTime horaEntrada) {
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
    // SERIALIZACIÓN (.txt)
    // =========================
    public String toFile() {
        // Almacenamos usando el formato ISO estándar nativo para asegurar compatibilidad estricta
        return placa + ";" + tipo.name() + ";" + horaEntrada.toString();
    }

    // =========================
    // DESERIALIZACIÓN (.txt)
    // =========================
    public static Vehiculo fromFile(String linea) {
        if (linea == null || linea.isBlank()) {
            return null;
        }

        try {
            String[] d = linea.split(";");

            if (d.length < 3) {
                return null; // Línea incompleta o corrupta
            }

            String placaRecuperada = d[0].toUpperCase().trim();
            TipoVehiculo tipoRecuperado = TipoVehiculo.valueOf(d[1].toUpperCase().trim());
            LocalDateTime fechaRecuperada = LocalDateTime.parse(d[2].trim());

            return new Vehiculo(placaRecuperada, tipoRecuperado, fechaRecuperada);

        } catch (Exception e) {
            // Captura errores de parseo de fechas o enums corruptos sin tumbar la app
            System.out.println("⚠️ Error al deserializar vehículo en línea: [" + linea + "]. Saltando registro.");
            return null;
        }
    }

    // =========================
    // PRESENTACIÓN
    // =========================
    public void mostrarInformacion() {
        System.out.println("----------------------");
        System.out.println("Placa   : " + placa);
        System.out.println("Tipo    : " + tipo);
        System.out.println("Entrada : " + horaEntrada.format(FORMATO_FECHA));
    }
}