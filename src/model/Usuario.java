package model;

/**
 * Modelo que mapea la tabla 'usuarios' de la base de datos.
 */
public class Usuario {
    private int id;
    private String username;
    private String password;
    private String rol;

    // Constructor completo
    public Usuario(int id, String username, String password, String rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // Getters (getId evita el warning de variable no usada)
    public int getId()          { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRol()      { return rol; }

    // Setters
    public void setId(int id)                { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRol(String rol)           { this.rol = rol; }
}