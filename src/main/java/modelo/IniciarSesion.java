
package modelo;


public class IniciarSesion {
    
    private int id;
    private String email;
    private String usuario;
    private String contrasena;
    private String rolUsuario;
    
    
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

    public String getRol() {return rolUsuario;}
    public void setRol(String rolUsuario) {this.rolUsuario=rolUsuario; }   
}
