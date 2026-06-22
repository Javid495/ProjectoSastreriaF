package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import getsSets.Registro;

public class RegistroDAO {
    
    public boolean registrar(Registro user, int RolCliente){
    
        String sqlRegistro = "insert into Registro(Registro_Usuario ,Registro_Contraseña, Registro_Email, Registro_Telefono) values(?, ?, ?, ?)";
        String sqlUsuario = "insert into Usuarios(Registro_id, Permisos_Roles_id) values (?, ?)";
        
        Connection con = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // Iniciamos la burbuja transaccional
            
            // 1. Insertar en la tabla Registro
            try(PreparedStatement psReg = con.prepareStatement(sqlRegistro, Statement.RETURN_GENERATED_KEYS)){
                psReg.setString(1, user.getUsuario());
                psReg.setString(2, user.getContrasena());
                psReg.setString(3, user.getEmail());
                
                // 🌟 CORREGIDO: Usamos setLong directo para que sea compatible con el BIGINT de la BD
                psReg.setLong(4, user.getTelefono()); 
            
                int filasReg = psReg.executeUpdate(); 
            
                if(filasReg > 0 ){
                    ResultSet rs = psReg.getGeneratedKeys();
                
                    if (rs.next()){
                        int lastRegistroId = rs.getInt(1); 
                        
                        // 2. Insertar en la tabla Usuarios obteniendo SU PROPIO ID GENERADO
                        try(PreparedStatement psUser = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)){
                        
                            psUser.setInt(1, lastRegistroId);
                            psUser.setInt(2, RolCliente);
                            int filasUser = psUser.executeUpdate();
                            
                            if (filasUser > 0) {
                                ResultSet rsUser = psUser.getGeneratedKeys();
                                if (rsUser.next()) {
                                    int idUsuarioVerdadero = rsUser.getInt(1); 
                                    
                                    // 3. Registrar en la bitácora compartiendo la MISMA CONEXIÓN
                                    HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
                                    // 🌟 ¡Aquí está la magia! Le pasamos 'con' como primer parámetro
                                    historialDAO.registrarAccion(
                                        con, 
                                        idUsuarioVerdadero, 
                                        "REGISTRO", 
                                        "Registro", 
                                        lastRegistroId, 
                                        "El usuario creó una cuenta nueva en la plataforma con éxito."
                                    );
                                }
                            }
                        }
                    }
                }
                
                // Si todo se ejecutó perfectamente dentro de la misma conexión, hacemos oficial la transacción
                con.commit();
                return true;
            }
  
        } 
        
        catch (SQLException e) {
            // Si cualquiera de los 3 pasos falla, el rollback cancelará absolutamente todo
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error en la transacción de registro: " + e.getMessage());
            return false;
        } 
        
        finally {
            // Cerramos la conexión principal de manera segura al terminar
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}