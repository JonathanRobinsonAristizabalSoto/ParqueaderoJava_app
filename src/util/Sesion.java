package util;

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
     * Evalúa de forma segura si la sesión cuenta con privilegios administrativos.
     */
    public static boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rolActual);
    }

    /**
     * Destruye las variables de estado limpiando la memoria al salir de la aplicación.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        rolActual = null;
    }
}