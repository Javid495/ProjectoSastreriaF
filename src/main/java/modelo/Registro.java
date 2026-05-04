
package modelo;


public class Registro {
    
    private String email;
    private String usuario;
    private String contrasena;
    private int telefono;
    
    
    public Registro() {}
    
    //Establecer nuestros getters y setters
    
    public String getUsuario() {return usuario;}
    public void setUsuario(String usuario) {this.usuario=usuario; }
    
    public String getContrasena() {return contrasena;}
    public void setContrasena(String contrasena) {this.contrasena=contrasena; }
    
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email=email; }
    
    public int getTelefono() {return telefono;}
    public void setTelefono(int telefono) {this.telefono= telefono; }
    
}
