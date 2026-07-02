package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Dtos.IniciarSesion;

// DAO que gestiona el proceso de iniciar sesión
public class LoginDAO {
     
    public IniciarSesion validarUsuario(String identificador, String contrasena) {
        
        // Indicamos el comando o la petición que queremos que mysql ejecute
        String sql = "SELECT r.*, u.Usuarios_id, u.Permisos_roles_id, u.Usuario_imagen " +
                     "FROM Registro r " + 
                     "JOIN Usuarios u ON r.Registro_id = u.Registro_id " + 
                     "WHERE (r.Registro_Usuario = ? OR r.Registro_Email = ?) AND r.Registro_Contraseña = ?;";
      
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        
        try {
            // 1. Establecemos conexión con la base de datos
            con = ClaseConexion.getConexion();
            
            // 2. Activamos el control transaccional
            con.setAutoCommit(false);
            
            // Preparamos la sintaxis mysql
            ps = con.prepareStatement(sql);
            
            // Reemplazamos los signos de ? en la variable sql con los datos de la consulta
            ps.setString(1, identificador);
            ps.setString(2, identificador);
            ps.setString(3, contrasena);
          
            result = ps.executeQuery();
          
            if (result.next()) {
                // En caso de encontrar coincidencias en la base de datos se crea el objeto de sesión
                IniciarSesion ver = new IniciarSesion();
                ver.setId(result.getInt("Usuarios_id"));
                ver.setEmail(result.getString("Registro_Email"));
                ver.setUsuario(result.getString("Registro_Usuario"));
                ver.setRolUsuario(result.getInt("Permisos_roles_id"));
                ver.setImagen(result.getString("Usuario_imagen"));
                
                // 🌟 3. Registramos la acción reutilizando de forma segura la misma conexión 'con'
                HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
                historialDAO.registrarAccion(
                    con, // 👈 Pasamos la conexión activa aquí
                    ver.getId(), 
                    "LOGIN", 
                    "Usuarios", 
                    ver.getId(), 
                    "El usuario inició sesión en el sistema."
                );
                
                // 4. Consolidamos la operación completa (Si el historial falla, el login no se procesa)
                con.commit();
                
                System.out.println("🔐 [LoginDAO] Sesión iniciada e historial registrado para el ID: " + ver.getId());
                return ver;
            }
          
        } 
        
        catch (SQLException e) {
            System.out.println("❌ Error en la validación del usuario al momento de hacer el login: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } 
        
        finally {
            // 5. Cierre seguro y manual de recursos para evitar conexiones colgadas en Tomcat
            try {
                if (result != null) result.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
            
            catch (SQLException e) { e.printStackTrace(); }
        }
        
        return null;
    }
}
