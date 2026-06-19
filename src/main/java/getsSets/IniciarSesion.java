
package getsSets;


public class IniciarSesion {
    
    private int id;
    private String email;
    private String usuario;
    private String contrasena;
    private int rolUsuario;
    private String imagen;
    
    // Geters y setters que haran de cables para almacenar y guardar los 
    //dato de la base de datos
    public IniciarSesion() {}
    
    //Establecer nuestros getters y setters
    
    public int getId() {return id;}
    public void setId(int id) {this.id=id; }
    
    public String getUsuario() {return usuario;}
    public void setUsuario(String usuario) {this.usuario=usuario; }
    
    public String getContrasena() {return contrasena;}
    public void setContrasena(String contrasena) {this.contrasena=contrasena; }
    
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email=email; }

    public int getRolUsuario() {
        return rolUsuario;
    }

    public void setRolUsuario(int rolUsuario) {
        this.rolUsuario = rolUsuario;
    }

      

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
