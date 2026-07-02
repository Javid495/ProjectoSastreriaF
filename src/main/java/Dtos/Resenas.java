package Dtos;

public class Resenas {
    private int id;
    private int usuarioId;
    private int prendaId;
    private String descripcion;
    private String imagenResena;
    // Campos agregados para la vista del cliente
    private String nombreUsuario;
    private String avatarUsuario;

    public Resenas() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getPrendaId() { return prendaId; }
    public void setPrendaId(int prendaId) { this.prendaId = prendaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenResena() { return imagenResena; }
    public void setImagenResena(String imagenResena) { this.imagenResena = imagenResena; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getAvatarUsuario() { return avatarUsuario; }
    public void setAvatarUsuario(String avatarUsuario) { this.avatarUsuario = avatarUsuario; }

}
