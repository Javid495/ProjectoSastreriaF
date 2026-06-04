package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MostrarPedidosAdminDAO {

    /**
     * Trae todos los pedidos del sistema unificados para el tablero del administrador.Une las compras de catálogo y los diseños hechos desde cero.
     * 
     */
    public List<String[]> listarPedidosParaAdmin() {
    List<String[]> lista = new ArrayList<>();
    
    // 💡 Usamos LEFT JOIN en ConfirmarPago para evitar que se oculten filas por nulos
    String sql = "SELECT pe.Pedido_id, pe.Pedido_FechaInicio, pe.Pedido_Estado, pe.Pedido_TCompra, " +
                 "dpm.Detalles_medidas, cp.ConfirmarPago_Fecha " +
                 "FROM Pedidos pe " +
                 "LEFT JOIN ConfirmarPago cp ON pe.ConfirmarPago_id = cp.ConfirmarPago_id " +
                 "LEFT JOIN CotizacionPedido cot ON cp.CotizacionPedido_id = cot.CotizacionPedido_id " +
                 "LEFT JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                 "ORDER BY pe.Pedido_id DESC;";
    
    try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            String[] fila = new String[5];
            fila[0] = String.valueOf(rs.getInt("Pedido_id"));
            
            String fechaInicio = rs.getString("Pedido_FechaInicio");
            String fechaPago = rs.getString("ConfirmarPago_Fecha");
            fila[1] = (fechaInicio != null) ? fechaInicio : (fechaPago != null ? fechaPago : "Sin fecha");
            
            // Sanitizamos la lectura del estado eliminando espacios molestos
            fila[2] = rs.getString("Pedido_Estado") != null ? rs.getString("Pedido_Estado").toLowerCase().trim() : "pendiente";
            fila[3] = rs.getString("Pedido_TCompra") != null ? rs.getString("Pedido_TCompra") : "No especificado";
            fila[4] = rs.getString("Detalles_medidas") != null ? rs.getString("Detalles_medidas") : "N/A (Compra Catálogo)";
            
            lista.add(fila);
        }
    } 
    
    catch (Exception e) {
        System.out.println("❌ Error listando pedidos en AdminPedidosDAO: " + e.getMessage());
        e.printStackTrace();
    }

    return lista;
    }

    /**
     * Modifica el estado del pedido en la base de datos (pendiente, elaboracion, entregar).
     */
    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado) {
        String sql = "UPDATE Pedidos SET Pedido_Estado = ? WHERE Pedido_id = ?;";
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("❌ Error al modificar estado del pedido #" + idPedido + " en AdminPedidosDAO: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> obtenerDetalleCatalogo(int idPedido) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, String>> prendas = new ArrayList<>();
        
        String sqlInfoGeneral = "SELECT cp.ConfirmarPago_Fecha, reg.Registro_Email, pe.Pedido_TCompra " +
                                "FROM Pedidos pe " +
                                "JOIN ConfirmarPago cp ON pe.ConfirmarPago_id = cp.ConfirmarPago_id " +
                                "JOIN DetallesCarrito dc ON cp.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                                "JOIN Carrito carr ON dc.Carrito_id = carr.Carrito_id " +
                                "JOIN Usuarios u ON carr.Usuarios_id = u.Usuarios_id " +
                                "JOIN Registro reg ON u.Registro_id = reg.Registro_id " +
                                "WHERE pe.Pedido_id = ? LIMIT 1;";

        String sqlPrendas = "SELECT pr.Prenda_nombre, pr.Prenda_valor, pr.Prenda_talla, dc.Detalles_total, " +
                            "(SELECT img.Imagenes_link FROM imagenes img WHERE img.Prenda_id = pr.Prenda_id LIMIT 1) AS Imagen " +
                            "FROM Pedidos pe " +
                            "JOIN ConfirmarPago cp ON pe.ConfirmarPago_id = cp.ConfirmarPago_id " +
                            "JOIN DetallesCarrito dc_main ON cp.DetallesCarrito_id = dc_main.DetallesCarrito_Id " +
                            "JOIN DetallesCarrito dc ON dc_main.Carrito_id = dc.Carrito_id " +
                            "JOIN Prendas pr ON dc.Prendas_id = pr.Prenda_id " +
                            "WHERE pe.Pedido_id = ?;";

        try (Connection con = ClaseConexion.getConexion()) {
            // 1. Cargar metadatos generales
            try (PreparedStatement ps = con.prepareStatement(sqlInfoGeneral)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resultado.put("fecha", rs.getString("ConfirmarPago_Fecha"));
                        resultado.put("email", rs.getString("Registro_Email"));
                        resultado.put("tipo", rs.getString("Pedido_TCompra"));
                    }
                }
            }
            // 2. Cargar lista de prendas compradas en ese carrito
            double totalAcumulado = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlPrendas)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> p = new HashMap<>();
                        p.put("nombre", rs.getString("Prenda_nombre"));
                        p.put("precio", String.valueOf(rs.getDouble("Prenda_valor")));
                        p.put("talla", rs.getString("Prenda_talla"));
                        p.put("imagen", rs.getString("Imagen") != null ? rs.getString("Imagen") : "images/Perfil/Ellipse 14.png");
                        prendas.add(p);
                        totalAcumulado += rs.getDouble("Detalles_total");
                    }
                }
            }
            resultado.put("total", totalAcumulado);
            resultado.put("prendas", prendas);

        } catch (Exception e) {
            System.out.println("❌ Error detalle catálogo: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene los detalles de un pedido HECHO A MEDIDA desde cero
     */
    public Map<String, Object> obtenerDetalleAMedida(int idPedido) {
        Map<String, Object> resultado = new HashMap<>();
        String sql = "SELECT pe.Pedido_TCompra, cp.ConfirmarPago_Fecha, reg.Registro_Email, " +
                     "dpm.Detalles_TPrenda, dpm.Detalles_Tela, dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia, " +
                     "cot.Cotizacion_Valor " +
                     "FROM Pedidos pe " +
                     "JOIN ConfirmarPago cp ON pe.ConfirmarPago_id = cp.ConfirmarPago_id " +
                     "JOIN CotizacionPedido cot ON cp.CotizacionPedido_id = cot.CotizacionPedido_id " +
                     "JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                     "JOIN Usuarios u ON dpm.Usuario_id = u.Usuarios_id " +
                     "JOIN Registro reg ON u.Registro_id = reg.Registro_id " +
                     "WHERE pe.Pedido_id = ?;";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado.put("tipo", rs.getString("Pedido_TCompra"));
                    resultado.put("fecha", rs.getString("ConfirmarPago_Fecha"));
                    resultado.put("email", rs.getString("Registro_Email"));
                    resultado.put("prenda", rs.getString("Detalles_TPrenda"));
                    resultado.put("tela", rs.getString("Detalles_Tela"));
                    resultado.put("medidas", rs.getString("Detalles_medidas"));
                    resultado.put("descripcion", rs.getString("Detalles_Descripcion"));
                    resultado.put("imagen", rs.getString("Detalles_ImagenReferencia"));
                    resultado.put("total", rs.getDouble("Cotizacion_Valor"));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error detalle A Medida: " + e.getMessage());
        }
        return resultado;
    }
}
