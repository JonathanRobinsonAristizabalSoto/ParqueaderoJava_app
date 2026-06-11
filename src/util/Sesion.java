package util;

/**
 * Gestiona el estado de la sesión de usuario en el sistema.
 * Utiliza un patrón Singleton estático para persistir la identidad durante la ejecución.
 */
public class Sesion {
    private static String usuarioActual;
    private static String rolActual;

    /**
     * Inicializa las variables globales de estado con las credenciales validadas.
     */
    public static void iniciarSesion(String username, String rol) {
        usuarioActual = username;
        rolActual = rol;
    }

    /**
     * Retorna el nombre del usuario logueado actualmente.
     */
    public static String getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Retorna el rol asignado al usuario en sesión activa.
     */
    public static String getRolActual() {
        return rolActual;
    }

    /**
     * Verifica si existe una sesión activa actualmente.
     */
    public static boolean estaActiva() {
        return usuarioActual != null;
    }

    /**
     * Evalúa de forma segura si la sesión cuenta con privilegios administrativos.
     */
    public static boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rolActual);
    }

    /**
     * Destruye las variables de estado limpiando la memoria al salir o cerrar sesión.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        rolActual = null;
    }
}