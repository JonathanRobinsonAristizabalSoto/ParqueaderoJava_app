import service.Parqueadero;
import util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Punto de entrada del sistema Parqueadero optimizado para MySQL
 */
public class Main {

    public static void main(String[] args) {

        // --- BLOQUE DE PRUEBA DE CONEXIÓN INICIAL A MYSQL ---
        try (Connection testCon = ConexionDB.getConexion()) {
            if (testCon != null) {
                System.out.println("\n=============================================");
                System.out.println("✅ ¡CONEXIÓN EXITOSA A MYSQL EN XAMPP CONECTADA!");
                System.out.println("=============================================");
            }
        } catch (SQLException e) {
            System.out.println("\n=============================================");
            System.out.println("❌ ERROR CRÍTICO: No se pudo conectar a MySQL.");
            System.out.println("Verifica que XAMPP (MySQL) esté encendido.");
            System.out.println("Detalle: " + e.getMessage());
            System.out.println("=============================================");
            return; // Detiene la ejecución si no hay base de datos activa
        }
        // -----------------------------------------------------

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
                System.out.println("8. Buscar historial por placa");
                System.out.println("9. Reporte financiero");
                System.out.println("10. Ranking de vehículos");
                System.out.println("11. Configuración del sistema ⚙️");
                System.out.println("12. Salir");
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
                        String placa = sc.nextLine().trim().toUpperCase(); // Normalización nativa

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
                        p.retirarVehiculo(sc.nextLine().trim().toUpperCase());
                    }

                    case 4 -> p.mostrarHistorial();

                    case 5 -> {
                        System.out.println("\n===== BUSCAR VEHÍCULO =====");
                        System.out.print("Placa: ");
                        p.buscarVehiculo(sc.nextLine().trim().toUpperCase());
                    }

                    case 6 -> p.mostrarEstado();

                    case 7 -> p.mostrarEstadisticas();

                    case 8 -> {
                        System.out.println("\n===== BUSCAR HISTORIAL POR PLACA =====");
                        System.out.print("Placa: ");
                        p.buscarHistorialPorPlaca(sc.nextLine().trim().toUpperCase());
                    }

                    case 9 -> p.mostrarReporteFinanciero();

                    case 10 -> p.mostrarRankingVehiculos();

                    case 11 -> menuConfiguracion(sc);

                    case 12 -> {
                        System.out.println("\n👋 Saliendo del sistema...");
                        return;
                    }

                    default -> System.out.println("❌ Opción inválida");
                }
            }
        }
    }

    /**
     * Submenú dedicado a la gestión de tarifas directamente en la Base de Datos.
     */
    private static void menuConfiguracion(Scanner sc) {
        while (true) {
            System.out.println("\n===== CONFIGURACIÓN DEL SISTEMA =====");
            System.out.println("1. Ver tarifas actuales (Desde DB)");
            System.out.println("2. Cambiar tarifa de Carro");
            System.out.println("3. Cambiar tarifa de Moto");
            System.out.println("4. Volver al menú principal");
            System.out.println("=====================================");
            System.out.print("Seleccione una opción: ");

            if (!sc.hasNextInt()) {
                System.out.println("\n❌ Debes ingresar un número válido.");
                sc.nextLine();
                continue;
            }

            int opConfig = sc.nextInt();
            sc.nextLine();

            switch (opConfig) {
                case 1 -> mostrarTarifasDB();
                case 2 -> actualizarTarifaDB("CARRO", leerDoublePositivo(sc, "Ingrese nueva tarifa para CARROS por hora: $"));
                case 3 -> actualizarTarifaDB("MOTO", leerDoublePositivo(sc, "Ingrese nueva tarifa para MOTOS por hora: $"));
                case 4 -> {
                    System.out.println("Regresando al menú principal...");
                    return;
                }
                default -> System.out.println("❌ Opción inválida");
            }
        }
    }

    /**
     * Consulta en tiempo real las tarifas registradas en la tabla MySQL
     */
    private static void mostrarTarifasDB() {
        String sql = "SELECT * FROM tarifas";
        System.out.println("\n----- TARIFAS VIGENTES EN BASE DE DATOS -----");
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String tipo = rs.getString("tipo_vehiculo");
                double valor = rs.getDouble("valor_hora");
                String icono = tipo.equalsIgnoreCase("CARRO") ? "🚗" : "🏍️";
                System.out.printf("%s %-5s : $%,.0f por hora%n", icono, tipo, valor);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al leer las tarifas: " + e.getMessage());
        }
    }

    /**
     * Actualiza y persiste inmediatamente el valor por hora en caliente en MySQL
     */
    private static void actualizarTarifaDB(String tipo, double nuevaTarifa) {
        if (nuevaTarifa < 0) return;
        
        String sql = "UPDATE tarifas SET valor_hora = ? WHERE tipo_vehiculo = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDouble(1, nuevaTarifa);
            ps.setString(2, tipo);
            
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("✅ Tarifa de " + tipo.toLowerCase() + " actualizada en MySQL correctamente.");
            } else {
                System.out.println("❌ No se encontró el tipo de vehículo especificado en la DB.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al actualizar la tarifa en DB: " + e.getMessage());
        }
    }

    /**
     * Valida de forma estricta que las entradas numéricas de dinero sean correctas.
     */
    private static double leerDoublePositivo(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!sc.hasNextDouble()) {
                System.out.println("❌ Error: Debes ingresar un valor numérico válido.");
                sc.nextLine();
                continue;
            }
            double valor = sc.nextDouble();
            sc.nextLine();

            if (valor < 0) {
                System.out.println("❌ Error: La tarifa no puede ser un valor negativo.");
                continue;
            }
            return valor;
        }
    }
}