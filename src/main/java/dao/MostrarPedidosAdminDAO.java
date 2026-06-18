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
     * Trae todos los pedidos unificados para el tablero del administrador.
     * Conecta Pedidos con DetallesPedidos -> CotizacionPedido -> DetallesPedidosMedida
     */
public List<String[]> listarPedidosParaAdmin() {
    List<String[]> lista = new ArrayList<>();

    // 🔑 Agregamos pe.Pedido_TipoPedido al SELECT
    String sql = "SELECT DISTINCT pe.Pedido_id, pe.Pedido_FechaInicio, pe.Pedido_Estado, pe.Pedido_TotalCompra, " +
                 "pe.Pedido_TipoPedido, dpm.Detalles_medidas, reg.Registro_Email, dpm.Detalles_TPrenda " +
                 "FROM Pedidos pe " +
                 "LEFT JOIN Usuarios u ON pe.Usuario_id = u.Usuarios_id " +
                 "LEFT JOIN Registro reg ON u.Registro_id = reg.Registro_id " +
                 "LEFT JOIN DetallesPedidos dp ON pe.Pedido_id = dp.Pedido_id " +
                 "LEFT JOIN CotizacionPedido cot ON dp.CotizacionPedido_id = cot.CotizacionPedido_Id " +
                 "LEFT JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                 "ORDER BY pe.Pedido_id DESC;";

    try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String[] fila = new String[8]; // 🔥 CORREGIDO: Tamaño aumentado a 8

            fila[0] = String.valueOf(rs.getInt("Pedido_id"));
            
            String fechaInicio = rs.getString("Pedido_FechaInicio");
            fila[1] = (fechaInicio != null) ? fechaInicio : "Sin fecha";

            fila[2] = rs.getString("Pedido_Estado") != null ? rs.getString("Pedido_Estado").toLowerCase().trim() : "pendiente";
            fila[3] = rs.getString("Pedido_TotalCompra") != null ? rs.getString("Pedido_TotalCompra") : "0.00";
            fila[4] = rs.getString("Detalles_medidas") != null ? rs.getString("Detalles_medidas") : "N/A (Compra Catálogo)";

            String emailUser = rs.getString("Registro_Email");
            fila[5] = (emailUser != null) ? emailUser : "Anónimo";
            
            String tipoPrenda = rs.getString("Detalles_TPrenda");
            fila[6] = (tipoPrenda != null) ? tipoPrenda : "Catálogo";

            // 🌟 NUEVO ÍNDICE [7]: Tipo de pedido real de la tabla Pedidos ("A Medida" o "Catálogo")
            String tipoPedido = rs.getString("Pedido_TipoPedido");
            fila[7] = (tipoPedido != null) ? tipoPedido : "Catálogo";

            lista.add(fila);
        }
    } catch (Exception e) {
        System.out.println("❌ Error listando pedidos en AdminPedidosDAO: " + e.getMessage());
        e.printStackTrace();
    }
    return lista;
}

    /**
     * Modifica el estado del pedido en la base de datos.
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
    
    /**
     * Obtiene los detalles de un pedido de CATÁLOGO usando DetallesPedidos
     */
    public Map<String, Object> obtenerDetalleCatalogo(int idPedido) {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, String>> prendas = new ArrayList<>();
        
        // Relación directa: Pedidos -> Usuarios -> Registro
        String sqlInfoGeneral = "SELECT pe.Pedido_FechaInicio, reg.Registro_Email, pe.Pedido_TipoPedido, pe.Pedido_TotalCompra " +
                                "FROM Pedidos pe " +
                                "JOIN Usuarios u ON pe.Usuario_id = u.Usuarios_id " +
                                "JOIN Registro reg ON u.Registro_id = reg.Registro_id " +
                                "WHERE pe.Pedido_id = ? LIMIT 1;";

        // Relación directa: DetallesPedidos -> Prendas -> imagenes
        String sqlPrendas = "SELECT pr.Prenda_id, pr.Prenda_nombre, pr.Prenda_valor, pr.Prenda_talla, dp.Detalles_Cantidad, dp.Detalles_PrecioTotal, " +
                            "(SELECT img.Imagenes_link FROM imagenes img WHERE img.Prenda_id = pr.Prenda_id LIMIT 1) AS Imagen " +
                            "FROM DetallesPedidos dp " +
                            "JOIN Prendas pr ON dp.Prenda_id = pr.Prenda_id " +
                            "WHERE dp.Pedido_id = ?;";

        try (Connection con = ClaseConexion.getConexion()) {
            // 1. Cargar metadatos generales
            try (PreparedStatement ps = con.prepareStatement(sqlInfoGeneral)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resultado.put("fecha", rs.getString("Pedido_FechaInicio"));
                        resultado.put("email", rs.getString("Registro_Email"));
                        resultado.put("tipo", rs.getString("Pedido_TipoPedido"));
                        resultado.put("total", rs.getDouble("Pedido_TotalCompra"));
                    }
                }
            }
            
            // 2. Cargar lista de prendas compradas desde DetallesPedidos
            try (PreparedStatement ps = con.prepareStatement(sqlPrendas)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> p = new HashMap<>();
                        p.put("nombre", rs.getString("Prenda_nombre"));
                        p.put("precio", String.valueOf(rs.getDouble("Prenda_valor")));
                        p.put("talla", rs.getString("Prenda_talla"));
                        p.put("cantidad", String.valueOf(rs.getInt("Detalles_Cantidad")));
                        p.put("subtotal", String.valueOf(rs.getDouble("Detalles_PrecioTotal")));
                        p.put("imagen", rs.getString("Imagen") != null ? rs.getString("Imagen") : "images/Perfil/Ellipse 14.png");
                        prendas.add(p);
                    }
                }
            }
            resultado.put("prendas", prendas);

        } catch (Exception e) {
            System.out.println("❌ Error detalle catálogo admin: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Obtiene los detalles de un pedido HECHO A MEDIDA
     */
    public Map<String, Object> obtenerDetalleAMedida(int idPedido) {
        Map<String, Object> resultado = new HashMap<>();
        
        String sql = "SELECT pe.Pedido_TipoPedido, pe.Pedido_FechaInicio, reg.Registro_Email, " +
                     "dpm.Detalles_TPrenda, dpm.Detalles_Tela, dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia, " +
                     "cot.Cotizacion_Valor " +
                     "FROM Pedidos pe " +
                     "JOIN DetallesPedidos dp ON pe.Pedido_id = dp.Pedido_id " +
                     "JOIN CotizacionPedido cot ON dp.CotizacionPedido_id = cot.CotizacionPedido_Id " +
                     "JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                     "JOIN Usuarios u ON pe.Usuario_id = u.Usuarios_id " +
                     "JOIN Registro reg ON u.Registro_id = reg.Registro_id " +
                     "WHERE pe.Pedido_id = ?;";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado.put("tipo", rs.getString("Pedido_TipoPedido"));
                    resultado.put("fecha", rs.getString("Pedido_FechaInicio"));
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
            System.out.println("❌ Error detalle A Medida admin: " + e.getMessage());
        }
        return resultado;
    }
}
