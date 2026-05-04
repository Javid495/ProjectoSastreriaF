
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Registro;

public class RegistroDAO {
    
    public boolean registrar(Registro user, int RolCliente){
    
        //Parte de mysql para generar el registro
        String sqlRegistro = "insert into Registro(Registro_Usuario ,Registro_Contraseña, Registro_Email, Registro_Telefono) values(?, ?, ?, ?)";
        
        //Parte de mysql para generar el usuario
        String sqlUsuario = "insert into Usuarios(Registro_id, Permisos_Roles_id) values (?, ?)";
        
        Connection con= null;
        
        try{
        
            con = ClaseConexion.getConexion();
            
            //Evitamos que java guarde cambios automaticamente
            //hasta que demos orden
            con.setAutoCommit(false);
            
            //Se pregunta a la tabla Registro id Cual fue el id que se le asigno al nuevo registro 
            try(PreparedStatement psReg = con.prepareStatement(sqlRegistro, PreparedStatement.RETURN_GENERATED_KEYS)){
            
            //Se hace referencia a cada columnas de la tabla
                psReg.setString(1, user.getUsuario());
                psReg.setString(2, user.getContrasena());
                psReg.setString(3, user.getEmail());
                psReg.setInt(4, user.getTelefono());
            
                int filasReg = psReg.executeUpdate(); 
            
                if(filasReg > 0 ){
                    //seObtiene el nuevo id que hace el registro
                    ResultSet rs = psReg.getGeneratedKeys();
                
                    if (rs.next()){
                        int lastId = rs.getInt(1); //se guarda el id que se creo recientemente
                        
                        //se realiza la insercion a usuarios
                        try(PreparedStatement psUser = con.prepareStatement(sqlUsuario)){
                        
                            psUser.setInt(1, lastId);
                            psUser.setInt(2, RolCliente);
                            psUser.executeUpdate();
                        }
                    
                    }
                }
                
                //Se hace el guardado si no se presentaron errores
                con.commit();
                return true;
            }
  
        }
        
        //En caso de que ocurra algun error con la BD de mysql
        catch (SQLException e) {
            
            if (con != null) {
                //Si alguno de los datos es nulo se ejecuta el rolback
                //y evita que lo inserte en la tabla
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error en la transacción de registro: " + e.getMessage());
            return false;
        } 
    }
}
