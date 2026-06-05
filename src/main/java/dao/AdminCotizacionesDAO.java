package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Prendas;

public class AdminCotizacionesDAO {

    // 1. Cuenta las solicitudes que NO tienen un registro en CotizacionPedido (Siguen pendientes)
    public int contarPendientesPorCotizar() {
        // ?Hacemos LEFT JOIN; si cp.CotizacionPedido_Id es NULL, significa que el sastre no lo ha cotizado.
        String sql = "SELECT COUNT(*) FROM DetallesPedidosMedida dpm " +
                     "LEFT JOIN CotizacionPedido cp ON dpm.Detalles_PedidoMedida_id = cp.DetallesPedidosMedida_id " +
                     "WHERE cp.CotizacionPedido_Id IS NULL";
        
        //Es quien abre la tuberia con la base de datos
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Error contando cotizaciones: " + e.getMessage());
        }
        return 0;
    }
    
    public int contarNuevosPedidos() {
        String sql = "SELECT COUNT(*) FROM Pedidos WHERE Pedido_Estado = 'Pendiente'";
                     
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Error contando nuevos pedidos: " + e.getMessage());
        }
        return 0;
    }
    
    public List<Prendas> listarPrendasBajasStock(int limiteStock) {
    List<Prendas> lista = new ArrayList<>();
    String sql = "SELECT p.Prenda_id, p.Prenda_nombre, c.Categoria_nombre, p.Prenda_talla, "
               + "p.Prenda_valor, p.Prenda_stock, p.Prenda_estado, MIN(i.Imagenes_link) AS Prenda_imagen "
               + "FROM Prendas p "
               + "INNER JOIN Categoria c ON p.Categoria_id = c.Categoria_id "
               + "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id "
               + "WHERE p.Prenda_stock <= ? AND p.Prenda_estado = 'activa' "
               + "GROUP BY p.Prenda_id, c.Categoria_nombre "
               + "ORDER BY p.Prenda_stock ASC";
               
    try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setInt(1, limiteStock); // Aquí le pasas el número (ej. 5)
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Aquí construyes tu objeto Prenda y lo agregas a la lista
                Prendas prenda = new Prendas();
                prenda.setId(rs.getInt("Prenda_id"));
                prenda.setNombre(rs.getString("Prenda_nombre"));
                prenda.setCategoria(rs.getString("Categoria_nombre"));
                prenda.setTalla(rs.getString("Prenda_talla"));
                prenda.setValor(rs.getDouble("Prenda_valor"));
                prenda.setStock(rs.getInt("Prenda_stock"));
                prenda.setEstado(rs.getString("Prenda_estado"));
                prenda.setImagen(rs.getString("Prenda_imagen"));
                
                lista.add(prenda);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}

    // 2. Trae el listado de los pedidos que NO poseen cotización realizada
    public List<String[]> listarPedidosPorCotizar() {
        List<String[]> lista = new ArrayList<>();
        // 🔑 Modificamos el WHERE para buscar la ausencia de registro en CotizacionPedido
        String sql = "SELECT dpm.Detalles_PedidoMedida_id, r.Registro_Email, dpm.Detalles_TPrenda, " +
                     "dpm.Detalles_Tela, dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia " +
                     "FROM DetallesPedidosMedida dpm " +
                     "JOIN Usuarios u ON dpm.Usuario_id = u.Usuarios_id " +
                     "JOIN Registro r ON u.Registro_id = r.Registro_id " +
                     "LEFT JOIN CotizacionPedido cp ON dpm.Detalles_PedidoMedida_id = cp.DetallesPedidosMedida_id " +
                     "WHERE cp.CotizacionPedido_Id IS NULL " + // Cambiado aquí
                     "ORDER BY dpm.Detalles_PedidoMedida_id DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String[] fila = new String[7];
                fila[0] = String.valueOf(rs.getInt("Detalles_PedidoMedida_id"));
                fila[1] = rs.getString("Registro_Email");
                fila[2] = rs.getString("Detalles_TPrenda");
                fila[3] = rs.getString("Detalles_Tela");
                fila[4] = rs.getString("Detalles_medidas");
                fila[5] = rs.getString("Detalles_Descripcion");
                
                String img = rs.getString("Detalles_ImagenReferencia");
                fila[6] = (img != null) ? img.replace("\\", "\\\\") : "";
                
                lista.add(fila);
            }
        } catch (Exception e) {
            System.out.println("Error listando cotizaciones pendientes: " + e.getMessage());
        }
        return lista;
    }
        
    //Funcion Guardar Cotizaciones
    // 3. Guarda la cotización oficial emitida por el administrador
    public boolean guardarCotizacion(int idPedidoMedida, double precio, String fechaLimite, String comentario) {
        String sql = "INSERT INTO CotizacionPedido (DetallesPedidosMedida_id, Cotizacion_Valor, ComentarioAdmin, Cotizacion_FechaLimite) " +
                 "VALUES (?, ?, ?, ?)";
                 
        try (Connection con = ClaseConexion.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
        
            ps.setInt(1, idPedidoMedida);
            ps.setDouble(2, precio);
        
            // En caso de que no se escriba algun comentario
            if (comentario == null || comentario.trim().isEmpty()) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } 
            else {
                ps.setString(3, comentario);
            }
        
            // MySQL acepta nativamente el formato de cadena "YYYY-MM-DD" que envía el input de tipo date
            ps.setString(4, fechaLimite); 
        
            return ps.executeUpdate() > 0; // Retorna true si se insertó la fila con éxito
        
        } 
        catch (Exception e) {
            System.out.println("Error al insertar cotización en la base de datos: " + e.getMessage());
            return false;
        }
    }
}

