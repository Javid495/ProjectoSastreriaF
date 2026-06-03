package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import modelo.DetallesPedidoMedida;

public class PedidosMedidaDao{

    public boolean registrarSolicitudMedida(DetallesPedidoMedida solicitud) {
        //Creamos nuestra instruccion my sql
        String sql = "INSERT INTO DetallesPedidosMedida (Usuario_id, Detalles_medidas, Detalles_TPrenda, Detalles_Tela, Detalles_Descripcion, Detalles_ImagenReferencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        //Probamos si la conexion con la base de datos es correcta
        try (Connection con = ClaseConexion.getConexion();
             //Preparamos los datos para mandarlos
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, solicitud.getIdUsuario());
            ps.setString(2, solicitud.getMedidas());
            ps.setString(3, solicitud.getTipoPrenda());
            ps.setString(4, solicitud.getTela());
            ps.setString(5, solicitud.getDescripcion());
            ps.setString(6, solicitud.getImagenReferencia());
            
            return ps.executeUpdate() > 0;
           
        } 
        //En caos de algun error durante el procesamiento de datos
        catch (SQLException e) {
            System.out.println("Error insertando pedido personalizado en ModaS: " + e.getMessage());
            return false;
        }
    }
}
