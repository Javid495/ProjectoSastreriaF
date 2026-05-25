
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Pedidos;

public class PedidosClienteDAO {
    
    public List<Pedidos> obtenerPedidosUsuario(int idUsuario){
        List<Pedidos> lista = new ArrayList<>();
        
        //consulta a la base de datos mysql
        String sql = "SELECT p.Pedido_id, p.Pedido_FechaInicio, p.Pedido_TCompra, p.Pedido_Estado " +
            "FROM Pedidos p " +
            "JOIN ConfirmarPago cp ON p.ConfirmarPago_id = cp.ConfirmarPago_id " +
            "LEFT JOIN DetallesCarrito dc ON cp.DetallesCarrito_id = dc.DetallesCarrito_id " +
            "LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id " +
            "LEFT JOIN CotizacionPedido cot ON cp.CotizacionPedido_id = cot.CotizacionPedido_id " +
            "LEFT JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
            "WHERE c.Usuarios_id = ? OR dpm.Usuario_id = ? " +
            "ORDER BY p.Pedido_FechaInicio DESC;";
        
        //Hago la conexion con la bese de datos
        try (Connection con = ClaseConexion.getConexion();
             // Preparo y mando la consulta
             PreparedStatement ps = con.prepareStatement(sql)){
            
            //Mando el id del usuario a buscar
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()){
                
                while(rs.next()){
                    
                    //Referencio a mi modelo de prendas
                    Pedidos p  = new Pedidos();
                    
                    //Mapeo los datos 
                    p.setIdPedido(rs.getInt("Pedido_id"));
                    p.setFechaInicio(rs.getString("Pedido_FechaInicio"));
                    p.setTipoCompra(rs.getString("Pedido_TCompra"));
                    p.setEstadoPedido(rs.getString("Pedido_Estado"));
                    
                    lista.add(p);
                }
            }
        }
        catch (SQLException e){
            System.out.println("Hubo algun error en obtener la lista de pedidos del usuario" + idUsuario + ": " + e.getMessage());
        }
        return lista;
    
    }
    
}
