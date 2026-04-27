
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PruebaDAO {
    
    public boolean registrar(Prueba user){
    
    
        String sql = "insert into Prueba(PruebaNombre ,PruebaUser, PruebaContra, PruebaTelefono) values(?, ?, ?, ?)";
        
        try(Connection con = ClaseConexion.getConexion();PreparedStatement ps = con.prepareStatement(sql)){
            
            //Se hace referencia a cada columnas de la tabla
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getUsuario());
            ps.setString(3, user.getContrasena());
            ps.setInt(4, user.getTelefono());
            
            int Afectadas = ps.executeUpdate();
            return Afectadas > 0;
            
        }
        
        catch (SQLException e){
            System.err.println("Error al ingresar datos" + e.getMessage());
            return false;
        }
    }
}
