package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import getsSets.Pedidos;

//DAo encargado de los pedidos del catalogo

public class PedidosClienteDAO {
    
    // 1. OBTENER LOS PEDIDOS CABECERA DEL USUARIO (Catálogo y Medida entran aquí de forma directa)
    public List<Pedidos> obtenerPedidosUsuario(int idUsuario){
        List<Pedidos> lista = new ArrayList<>();
        
        // Consulta simplificada y corregida según tus columnas reales de la tabla 'Pedidos'
        String sql = "SELECT Pedido_id, Pedido_FechaInicio, Pedido_TipoPedido, Pedido_Estado, Pedido_TotalCompra " +
                     "FROM Pedidos " +
                     "WHERE Usuario_id = ? " +
                     "ORDER BY Pedido_FechaInicio DESC;";
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setInt(1, idUsuario);
            
            try (ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    Pedidos p  = new Pedidos();
                    
                    p.setIdPedido(rs.getInt("Pedido_id"));
                    p.setFechaInicio(rs.getString("Pedido_FechaInicio"));
                    p.setTipoCompra(rs.getString("Pedido_TipoPedido")); // Cambiado de Pedido_TCompra a Pedido_TipoPedido
                    p.setEstadoPedido(rs.getString("Pedido_Estado"));
                    
                    lista.add(p);
                }
            }
        }
        catch (SQLException e){
            System.out.println("❌ Error en obtenerPedidosUsuario (" + idUsuario + "): " + e.getMessage());
        }
        return lista;
    }
    
    // 2. DESGLOSE DE PRODUCTOS PARA COMPRAS DESDE EL CATÁLOGO
    public List<String[]> obtenerProductosPorPedido(int idPedido) {
        List<String[]> lista = new ArrayList<>();
        
        // Consulta corregida usando la tabla intermedia real 'DetallesPedidos'
        String sql = "SELECT p.Prenda_nombre, p.Prenda_valor, dp.Detalles_PrecioTotal, dp.Detalles_Cantidad, " +
                     "MIN(img.Imagenes_link) AS Imagen_link " +
                     "FROM DetallesPedidos dp " +
                     "JOIN Prendas p ON dp.Prenda_id = p.Prenda_id " +
                     "LEFT JOIN imagenes img ON p.Prenda_id = img.Prenda_id " +
                     "WHERE dp.Pedido_id = ? " +
                     "GROUP BY p.Prenda_id, p.Prenda_nombre, p.Prenda_valor, dp.Detalles_PrecioTotal, dp.Detalles_Cantidad;";
                     
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] registro = new String[5];
                    registro[0] = rs.getString("Prenda_nombre");
                    registro[1] = rs.getString("Prenda_valor");
                    registro[2] = rs.getString("Detalles_PrecioTotal"); // Cambiado a Detalles_PrecioTotal
                    registro[3] = String.valueOf(rs.getInt("Detalles_Cantidad")); // Cambiado a Detalles_Cantidad
                    
                    String rutaImg = rs.getString("Imagen_link");
                    if (rutaImg != null) {
                        rutaImg = rutaImg.replace("\\", "\\\\"); // Escapa barras inclinadas para no romper el JSON
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
    
    // 3. DESGLOSE DE DETALLES PARA COMPRAS Hechas A MEDIDA
    public String[] obtenerDetallesPedidoMedida(int idPedido) {
        String[] detalles = null;
    
        // Consulta corregida uniendo Pedidos -> DetallesPedidos -> CotizacionPedido -> DetallesPedidosMedida
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
