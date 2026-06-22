package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // 👈 Asegúrate de importar esto
import getsSets.Registro;

public class RegistroDAO {
    
    public boolean registrar(Registro user, int RolCliente){
    
        String sqlRegistro = "insert into Registro(Registro_Usuario ,Registro_Contraseña, Registro_Email, Registro_Telefono) values(?, ?, ?, ?)";
        String sqlUsuario = "insert into Usuarios(Registro_id, Permisos_Roles_id) values (?, ?)";
        
        Connection con = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false);
            
            // 1. Insertar en la tabla Registro
            try(PreparedStatement psReg = con.prepareStatement(sqlRegistro, Statement.RETURN_GENERATED_KEYS)){
                psReg.setString(1, user.getUsuario());
                psReg.setString(2, user.getContrasena());
                psReg.setString(3, user.getEmail());
                psReg.setString(4, String.valueOf(user.getTelefono())); // Si teléfono es String o int conviértelo según corresponda
            
                int filasReg = psReg.executeUpdate(); 
            
                if(filasReg > 0 ){
                    ResultSet rs = psReg.getGeneratedKeys();
                
                    if (rs.next()){
                        int lastRegistroId = rs.getInt(1); // ID de la tabla Registro
                        
                        // 2. Insertar en la tabla Usuarios obteniendo SU PROPIO ID GENERADO 👈 (Cambio Crítico)
                        try(PreparedStatement psUser = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)){
                        
                            psUser.setInt(1, lastRegistroId);
                            psUser.setInt(2, RolCliente);
                            int filasUser = psUser.executeUpdate();
                            
                            if (filasUser > 0) {
                                ResultSet rsUser = psUser.getGeneratedKeys();
                                if (rsUser.next()) {
                                    int idUsuarioVerdadero = rsUser.getInt(1); // 🌟 ¡Este es el Usuarios_id real!
                                    
                                    // 3. Registrar en la bitácora con privacidad e IDs correctos
                                    HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
                                    historialDAO.registrarAccion(
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
                
                con.commit();
                return true;
            }
  
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error en la transacción de registro: " + e.getMessage());
            return false;
        } 
    }
}