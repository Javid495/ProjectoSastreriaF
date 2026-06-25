package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import getsSets.Pedidos;

// DAO encargado de consultar las compras y pedidos históricos del cliente
public class PedidosClienteDAO {
    
    // ==========================================================================
    // 1. OBTENER LOS PEDIDOS CABECERA DEL USUARIO (Catálogo y Medida mediante JOINs)
    // ==========================================================================
    public List<Pedidos> obtenerPedidosUsuario(int idUsuario){
        List<Pedidos> lista = new ArrayList<>();
        
        // 💡 CORRECCIÓN CRÍTICA: Como no hay Usuario_id en Pedidos, unimos usando UNION 
        // para rastrear al usuario por el Carrito (Catálogo) o por la Solicitud (Medida)
        String sql = "SELECT p.Pedido_id, p.Pedido_FechaInicio, p.Pedido_TipoPedido, p.Pedido_Estado, p.Pedido_TotalCompra " +
                     "FROM Pedidos p " +
                     "JOIN DetallesPedidos dp ON p.Pedido_id = dp.Pedido_id " +
                     "JOIN DetallesCarrito dc ON dp.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                     "JOIN Carrito c ON dc.Carrito_id = c.Carrito_id " +
                     "WHERE c.Usuarios_id = ? " +
                     "UNION " +
                     "SELECT p.Pedido_id, p.Pedido_FechaInicio, p.Pedido_TipoPedido, p.Pedido_Estado, p.Pedido_TotalCompra " +
                     "FROM Pedidos p " +
                     "JOIN DetallesPedidos dp ON p.Pedido_id = dp.Pedido_id " +
                     "JOIN CotizacionPedido cot ON dp.CotizacionPedido_id = cot.CotizacionPedido_id " +
                     "JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                     "WHERE dpm.Usuario_id = ? " +
                     "ORDER BY Pedido_FechaInicio DESC;";
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)){
            
            // 💡 NOTA: Al usar UNION, tenemos dos comodines '?', por lo que enviamos el idUsuario dos veces
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Pedidos p = new Pedidos();
                    
                    p.setIdPedido(rs.getInt("Pedido_id"));
                    p.setFechaInicio(rs.getString("Pedido_FechaInicio"));
                    p.setTipoCompra(rs.getString("Pedido_TipoPedido")); 
                    p.setEstadoPedido(rs.getString("Pedido_Estado"));
                    
                    // Opcional: si tu clase Pedidos tiene setTotalCompra, puedes mapearlo aquí:
                    // p.setTotalCompra(rs.getDouble("Pedido_TotalCompra"));
                    
                    lista.add(p);
                }
            }
        }
        catch (SQLException e){
            System.out.println("❌ Error en obtenerPedidosUsuario (" + idUsuario + "): " + e.getMessage());
        }
        return lista;
    }
    
    // ==========================================================================
    // 2. DESGLOSE DE PRODUCTOS PARA COMPRAS DESDE EL CATÁLOGO
    // ==========================================================================
    public List<String[]> obtenerProductosPorPedido(int idPedido) {
        List<String[]> lista = new ArrayList<>();
        
        // 💡 CORRECCIÓN CRÍTICA: Cambiado para pasar correctamente por DetallesCarrito,
        // ya que DetallesPedidos no contiene las columnas 'Prenda_id' ni 'Cantidad' de forma directa.
        String sql = "SELECT pr.Prenda_nombre, pr.Prenda_valor, dp.Detalles_PrecioTotal, dc.Detalles_cantidad, " +
                     "MIN(img.Imagenes_link) AS Imagen_link " +
                     "FROM DetallesPedidos dp " +
                     "JOIN DetallesCarrito dc ON dp.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                     "JOIN Prendas pr ON dc.Prendas_id = pr.Prenda_id " +
                     "LEFT JOIN imagenes img ON pr.Prenda_id = img.Prenda_id " +
                     "WHERE dp.Pedido_id = ? " +
                     "GROUP BY pr.Prenda_id, pr.Prenda_nombre, pr.Prenda_valor, dp.Detalles_PrecioTotal, dc.Detalles_cantidad;";
                     
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    
                    //Organiza los datos que me retorna la consulta anteriror
                    String[] registro = new String[5];
                    registro[0] = rs.getString("Prenda_nombre");
                    registro[1] = rs.getString("Prenda_valor");
                    registro[2] = rs.getString("Detalles_PrecioTotal"); 
                    registro[3] = String.valueOf(rs.getInt("Detalles_cantidad")); // dc.Detalles_cantidad en minúscula como en tu BD
                    
                    String rutaImg = rs.getString("Imagen_link");
                    if (rutaImg != null) {
                        rutaImg = rutaImg.replace("\\", "\\\\"); 
                    }
                    registro[4] = rutaImg;
                    
                    lista.add(registro);
                }
            }
        } 
        catch (Exception e) {
            System.out.println("❌ Error consultando desglose de catálogo (obtenerProductosPorPedido): " + e.getMessage());
        }
        return lista;
    }
    
    // ==========================================================================
    // 3. DESGLOSE DE DETALLES PARA COMPRAS HECHAS A MEDIDA
    // ==========================================================================
    public String[] obtenerDetallesPedidoMedida(int idPedido) {
        String[] detalles = null;
    
        // 🔍 REVISIÓN: Esta consulta original tuya estaba perfecta y sigue los JOINs correctos 
        // Pedidos -> DetallesPedidos -> CotizacionPedido -> DetallesPedidosMedida
        String sql = "SELECT dpm.Detalles_TPrenda, dpm.Detalles_Tela, cot.Cotizacion_Valor, cot.ComentarioAdmin " +
                     "FROM DetallesPedidos dp " +
                     "JOIN CotizacionPedido cot ON dp.CotizacionPedido_id = cot.CotizacionPedido_id " +
                     "JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                     "WHERE dp.Pedido_id = ?;";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
        
            ps.setInt(1, idPedido);
        
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    detalles = new String[4];
                    detalles[0] = rs.getString("Detalles_TPrenda");      
                    detalles[1] = rs.getString("Detalles_Tela");        
                    detalles[2] = rs.getString("Cotizacion_Valor");    
                    detalles[3] = rs.getString("ComentarioAdmin");     
                }
            }
        } 
        catch (Exception e) {
            System.out.println("❌ Error consultando detalles del pedido a medida: " + e.getMessage());
        }
        
        return detalles; 
    }
}