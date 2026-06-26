package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import getsSets.DetallesPedidoMedida;

// DAO que maneja los pedidos a medida de los usuarios hecho desde cero
public class PedidosMedidaDao {

    // ==========================================================================
    // 🧵 REGISTRAR SOLICITUD DESDE CERO CON BITÁCORA TRANSACCIONAL
    // ==========================================================================
    public boolean registrarSolicitudMedida(DetallesPedidoMedida solicitud) {
        String sql = "INSERT INTO DetallesPedidosMedida (Usuario_id, Detalles_medidas, Detalles_TPrenda, Detalles_Tela, Detalles_Descripcion, Detalles_ImagenReferencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // 🌟 Iniciamos la transacción para asegurar ambos inserts
            
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, solicitud.getIdUsuario());
            ps.setString(2, solicitud.getMedidas());
            ps.setString(3, solicitud.getTipoPrenda());
            ps.setString(4, solicitud.getTela());
            ps.setString(5, solicitud.getDescripcion());
            ps.setString(6, solicitud.getImagenReferencia());
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int idPedidoMedidaGenerado = rs.getInt(1);
                    
                    // 🌟 Reutilizamos la misma conexión activa 'con' para escribir en la bitácora
                    HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
                    historialDAO.registrarAccion(
                        con, // 👈 Pasamos la conexión activa
                        solicitud.getIdUsuario(), 
                        "PEDIDO_MEDIDA", 
                        "DetallesPedidosMedida", 
                        idPedidoMedidaGenerado, 
                        "El usuario solicitó una cotización para un diseño personalizado hecho desde cero."
                    );
                }
                
                // Si ambos pasos fueron exitosos, consolidamos los cambios en la BD
                con.commit();
                return true;
            }
            
            return false;
           
        } catch (SQLException e) {
            System.out.println("❌ Error insertando pedido personalizado en ModaS: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Cerramos de forma segura todos los recursos abiertos
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    //Metodo que lita las cotizaciones que ya han sido aprobadas por el administrador segunel susuario

    public List<String[]> listarCotizacionesUsuario(int idUsuario) {
        List<String[]> lista = new ArrayList<>();

        // Consulta corregida incluyendo el ID real de la cotización al final
        String sql = "SELECT dpm.Detalles_PedidoMedida_id, dpm.Detalles_TPrenda, dpm.Detalles_Tela, " +
                     "dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia, " +
                     "cp.Cotizacion_Valor, cp.ComentarioAdmin, cp.Cotizacion_FechaLimite, " +
                     "cp.CotizacionPedido_Id " + // 🌟 Agregado aquí
                     "FROM DetallesPedidosMedida dpm " +
                     "INNER JOIN CotizacionPedido cp ON dpm.Detalles_PedidoMedida_id = cp.DetallesPedidosMedida_id " +
                     "WHERE dpm.Usuario_id = ? " +
                     "AND cp.CotizacionPedido_Id NOT IN ( " +
                     "    SELECT dp.CotizacionPedido_id " +
                     "    FROM DetallesPedidos dp " +
                     "    WHERE dp.CotizacionPedido_id IS NOT NULL " +
                     ") " +
                     "ORDER BY cp.CotizacionPedido_Id DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 🌟 Aumentamos el tamaño a 10 para no alterar tus índices del 0 al 8
                    String[] fila = new String[10]; 

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

                    // 🌟 Guardamos el ID real de la cotización en la última posición
                    fila[9] = String.valueOf(rs.getInt("CotizacionPedido_Id")); 

                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al listar cotizaciones del usuario: " + e.getMessage());
        }
        return lista;
    }
}