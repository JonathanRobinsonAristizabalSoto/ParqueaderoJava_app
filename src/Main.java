// Ruta: src/Main.java

import service.Parqueadero;
import java.util.Scanner;

/**
 * Punto de entrada del sistema Parqueadero
 */
public class Main {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in)) {

            Parqueadero p = new Parqueadero();

            while (true) {

                System.out.println("\n=================================");
                System.out.println("      SISTEMA DE PARQUEADERO");
                System.out.println("=================================");
                System.out.println("1. Ingresar vehículo");
                System.out.println("2. Listar vehículos");
                System.out.println("3. Retirar vehículo");
                System.out.println("4. Ver historial");
                System.out.println("5. Buscar vehículo");
                System.out.println("6. Estado del parqueadero");
                System.out.println("7. Estadísticas");
                System.out.println("8. Salir");
                System.out.println("=================================");
                System.out.print("Seleccione una opción: ");

                if (!sc.hasNextInt()) {
                    System.out.println("\n❌ Debes ingresar un número válido.");
                    sc.nextLine();
                    continue;
                }

                int op = sc.nextInt();
                sc.nextLine();

                switch (op) {

                    case 1 -> {

                        System.out.println("\n===== INGRESO VEHÍCULO =====");

                        System.out.print("Placa: ");
                        String placa = sc.nextLine().trim();

                        System.out.println("\nSeleccione tipo de vehículo:");
                        System.out.println("1. Carro");
                        System.out.println("2. Moto");
                        System.out.print("Opción: ");

                        if (!sc.hasNextInt()) {
                            System.out.println("❌ Opción inválida");
                            sc.nextLine();
                            continue;
                        }

                        int tipoOp = sc.nextInt();
                        sc.nextLine();

                        String tipo;

                        switch (tipoOp) {
                            case 1 -> tipo = "CARRO";
                            case 2 -> tipo = "MOTO";
                            default -> {
                                System.out.println("❌ Tipo inválido");
                                continue;
                            }
                        }

                        p.ingresarVehiculo(placa, tipo);
                    }

                    case 2 -> p.mostrarVehiculos();

                    case 3 -> {

                        System.out.println("\n===== RETIRAR VEHÍCULO =====");
                        System.out.print("Placa: ");

                        p.retirarVehiculo(sc.nextLine().trim());
                    }

                    case 4 -> p.mostrarHistorial();

                    case 5 -> {

                        System.out.println("\n===== BUSCAR VEHÍCULO =====");
                        System.out.print("Placa: ");

                        p.buscarVehiculo(sc.nextLine().trim());
                    }

                    case 6 -> p.mostrarEstado();

                    case 7 -> p.mostrarEstadisticas();

                    case 8 -> {
                        System.out.println("\n👋 Saliendo del sistema...");
                        return;
                    }

                    default -> System.out.println("❌ Opción inválida");
                }
            }
        }
    }
}