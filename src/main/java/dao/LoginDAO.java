
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.IniciarSesion;

public class LoginDAO {
    
    
    public IniciarSesion validarUsuario(String identificador, String contrasena){
    
        //Indicamos ell comando o la peticion que queremos que mysql ejecute
        //Esta peticion nos retorna un resultado
      String sql = "SELECT r.*, u.Usuarios_id, u.Permisos_roles_id, u.Usuario_imagen " +
                    "FROM Registro r " + 
                    "JOIN Usuarios u ON r.Registro_id = u.Registro_id " + 
                    "WHERE (r.Registro_Usuario = ? OR r.Registro_Email = ?) AND r.Registro_Contraseña = ?;";
      
      try(
          //Establecemos conexion con la base de datos
          Connection con = ClaseConexion.getConexion();
              
          //Preparamos la sintasis mysql
          PreparedStatement ps = con.prepareStatement(sql)){
      
          //Remplazamos los signos de ? en la variable sql con los datos de la consulta
          ps.setString(1, identificador);
          ps.setString(2, identificador);
          ps.setString(3, contrasena);
          
          try (ResultSet result = ps.executeQuery()){
          
              if (result.next ()){
              
                  //En caso de encontrar coincidencias en la base de datos
                  //Se creo un objeto con los datos del usuario
                  IniciarSesion ver = new IniciarSesion();
                  ver.setId(result.getInt("Usuarios_id"));
                  ver.setEmail(result.getString("Registro_Email"));
                  ver.setUsuario(result.getString("Registro_Usuario"));
                  ver.setRolUsuario(result.getInt("Permisos_roles_id"));
                  ver.setImagen(result.getString("Usuario_imagen"));
                  
                  //añadir la manipulacion de los elementos cuando el usuario este activo
                  return ver;
              }
          }
      
      
      }
          
      catch(SQLException e){
          System.out.println("Error en la validacion del usuario al momento de haccer el login intente nuevamente" + e.getMessage());
      }
      
    return null;
    }
    
}
