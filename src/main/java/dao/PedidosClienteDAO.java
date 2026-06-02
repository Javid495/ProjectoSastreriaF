
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
    
    public List<String[]> obtenerProductosPorPedido(int idPedido) {
    List<String[]> lista = new ArrayList<>();
    String sql = "SELECT p.Prenda_nombre, p.Prenda_valor, dc.Detalles_total, " +
                 "ROUND(dc.Detalles_total / p.Prenda_valor) AS Cantidad, " +
                 "MIN(img.Imagenes_link) AS Imagen_link " +
                 "FROM Pedidos pe " +
                 "JOIN ConfirmarPago cp ON pe.ConfirmarPago_id = cp.ConfirmarPago_id " +
                 "JOIN DetallesCarrito dc_ref ON cp.DetallesCarrito_id = dc_ref.DetallesCarrito_Id " +
                 "JOIN DetallesCarrito dc ON dc_ref.Carrito_id = dc.Carrito_id " +
                 "JOIN Prendas p ON dc.Prendas_id = p.Prenda_id " +
                 "LEFT JOIN imagenes img ON p.Prenda_id = img.Prenda_id " +
                 "WHERE pe.Pedido_id = ? " +
                 "GROUP BY p.Prenda_id, p.Prenda_nombre, p.Prenda_valor, dc.Detalles_total";
                 
    // Nota: Revisa si en tu base de datos tu llave primaria de pedidos se llama Pedido_id o idPedido.

    try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setInt(1, idPedido);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String[] registro = new String[5];
                registro[0] = rs.getString("Prenda_nombre");
                registro[1] = rs.getString("Prenda_valor");
                registro[2] = rs.getString("Detalles_total");
                registro[3] = String.valueOf(rs.getInt("Cantidad"));
                
                // Salvaguarda para las rutas de imágenes con contrabarras (\)
                String rutaImg = rs.getString("Imagen_link");
                if (rutaImg != null) {
                    rutaImg = rutaImg.replace("\\", "\\\\"); // Evita que rompa el JSON
                }
                registro[4] = rutaImg;
                
                lista.add(registro);
            }
        }
    } 
    catch (Exception e) {
        System.out.println("Error consultando desglose del pedido: " + e.getMessage());
    }
        return lista;
    }
    
}
