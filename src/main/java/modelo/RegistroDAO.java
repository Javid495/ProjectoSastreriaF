package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import Dtos.Registro;

public class RegistroDAO {
    
    public boolean registrar(Registro user, int RolCliente){
    
        String sqlRegistro = "insert into Registro(Registro_Usuario ,Registro_Contraseña, Registro_Email, Registro_Telefono) values(?, ?, ?, ?)";
        
        // 🛠️ MODIFICADO: Añadimos la columna Usuario_imagen a la consulta
        String sqlUsuario = "insert into Usuarios(Registro_id, Permisos_Roles_id, Usuario_imagen) values (?, ?, ?)";
        
        Connection con = null;
        
        try {
            con = ClaseConexion.getConexion();
            
            //En caso de algun error en las inserciones no me las guarde si no me retorne a este punto
            con.setAutoCommit(false); // Iniciamos la burbuja transaccional o un checkpoint
            
            // 1. Insertar en la tabla Registro
            try(PreparedStatement psReg = con.prepareStatement(sqlRegistro, Statement.RETURN_GENERATED_KEYS)){
                psReg.setString(1, user.getUsuario());
                psReg.setString(2, user.getContrasena());
                psReg.setString(3, user.getEmail());
                
                // Usamos setLong directo para que sea compatible con el BIGINT de la BD
                psReg.setLong(4, user.getTelefono()); 
            
                int filasReg = psReg.executeUpdate(); 
            
                if(filasReg > 0 ){
                    //El resultSet me guarda un objecto y se guarda una tabla o estrutura temporal
                    ResultSet rs = psReg.getGeneratedKeys();
                    
                    // . next inicia desde una fila 0 o si datos si hay un conjunto de eleemntos en la siguiente fila 
                    // Retorna true caso ccontrario false
                    //.next = Retorna un valor booleando si hay mas de un array de datos
                    if (rs.next()){
                        int lastRegistroId = rs.getInt(1); 
                        
                        // 2. Insertar en la tabla Usuarios obteniendo SU PROPIO ID GENERADO
                        try(PreparedStatement psUser = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)){
                        
                            psUser.setInt(1, lastRegistroId);
                            psUser.setInt(2, RolCliente);
                            
                            // 🌟 NUEVO: Asignamos la ruta por defecto usando "/" para entorno Web seguro
                            psUser.setString(3, "images/Perfil/Ellipse14.png");
                            
                            //Este parte del codigo me confirma si se realizo correctamente la insercion en la tabla usuarios
                            int filasUser = psUser.executeUpdate();
                            
                            if (filasUser > 0) {
                                ResultSet rsUser = psUser.getGeneratedKeys();
                                
                                //El .next() pregunta o verifica si hay fila 1
                                if (rsUser.next()) {
                                    int idUsuarioVerdadero = rsUser.getInt(1); 
                                    
                                    // 3. Registrar en la bitácora compartiendo la MISMA CONEXIÓN
                                    HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
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