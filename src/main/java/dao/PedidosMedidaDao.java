package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
    // Listar las cotizaciones listas y aprobadas por el sastre para un usuario específico
    public java.util.List<String[]> listarCotizacionesUsuario(int idUsuario) {
    java.util.List<String[]> lista = new java.util.ArrayList<>();
    
    // 🔑 Unimos la solicitud con su cotización correspondiente mediante INNER JOIN
        String sql = "SELECT dpm.Detalles_PedidoMedida_id, dpm.Detalles_TPrenda, dpm.Detalles_Tela, " +
                    "dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia, " +
                    "cp.Cotizacion_Valor, cp.ComentarioAdmin, cp.Cotizacion_FechaLimite " +
                    "FROM DetallesPedidosMedida dpm " +
                    "INNER JOIN CotizacionPedido cp ON dpm.Detalles_PedidoMedida_id = cp.DetallesPedidosMedida_id " +
                    "WHERE dpm.Usuario_id = ? " +
                    "ORDER BY cp.CotizacionPedido_Id DESC";

        try (Connection con = ClaseConexion.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
        
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] fila = new String[9];
                    fila[0] = String.valueOf(rs.getInt("Detalles_PedidoMedida_id"));
                    fila[1] = rs.getString("Detalles_TPrenda");
                    fila[2] = rs.getString("Detalles_Tela");
                    fila[3] = rs.getString("Detalles_medidas");
                    fila[4] = rs.getString("Detalles_Descripcion");
                
                    String img = rs.getString("Detalles_ImagenReferencia");
                    fila[5] = (img != null) ? img.replace("\\", "\\\\") : "";
                
                    fila[6] = String.valueOf(rs.getDouble("Cotizacion_Valor"));
                
                    String com = rs.getString("ComentarioAdmin");
                    fila[7] = (com != null) ? com : "Sin comentarios adicionales.";
                
                    fila[8] = String.valueOf(rs.getDate("Cotizacion_FechaLimite"));
                
                    lista.add(fila);
                }
            }
        }   
        catch (SQLException e) {
            System.out.println("Error al listar cotizaciones del usuario: " + e.getMessage());
        }
        return lista;
    }
    
}
