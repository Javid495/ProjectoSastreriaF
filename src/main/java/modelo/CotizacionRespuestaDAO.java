
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CotizacionRespuestaDAO {

    /**
     * Rompe vínculos y elimina una cotización junto con su solicitud a medida
     * usando un bloque transaccional atómico.
     */
    public boolean eliminarCotizacionYSolicitud(int idCotizacion) throws SQLException {
        Connection conn = null;
        PreparedStatement psGetSolicitud = null;
        PreparedStatement psUnlinkDetalles = null;
        PreparedStatement psDeleteCotizacion = null;
        PreparedStatement psDeleteSolicitud = null;
        ResultSet rs = null;
        boolean exito = false;

        try {
            conn = ClaseConexion.getConexion();
            if (conn == null) {
                return false;
            }

            // Transacción controlada
            conn.setAutoCommit(false);

            // 1. Obtener el ID de la solicitud original
            String sqlFindSolicitud = "SELECT DetallesPedidosMedida_id FROM CotizacionPedido WHERE CotizacionPedido_Id = ?";
            psGetSolicitud = conn.prepareStatement(sqlFindSolicitud);
            psGetSolicitud.setInt(1, idCotizacion);
            rs = psGetSolicitud.executeQuery();

            int idSolicitudMedida = -1;
            if (rs.next()) {
                idSolicitudMedida = rs.getInt("DetallesPedidosMedida_id");
            }

            // Si la cotización no existe, cancelamos todo de inmediato
            if (idSolicitudMedida == -1) {
                conn.rollback();
                return false;
            }

            // Romper el vínculo con DetallesPedidos para evitar errores de llave foránea
            String sqlUnlink = "UPDATE DetallesPedidos SET CotizacionPedido_id = NULL WHERE CotizacionPedido_id = ?";
            psUnlinkDetalles = conn.prepareStatement(sqlUnlink);
            psUnlinkDetalles.setInt(1, idCotizacion);
            psUnlinkDetalles.executeUpdate();

            // Borrar el hijo (CotizacionPedido)
            String sqlDeleteCotizacion = "DELETE FROM CotizacionPedido WHERE CotizacionPedido_Id = ?";
            psDeleteCotizacion = conn.prepareStatement(sqlDeleteCotizacion);
            psDeleteCotizacion.setInt(1, idCotizacion);
            psDeleteCotizacion.executeUpdate();

            // Borrar el padre (DetallesPedidosMedida)
            String sqlDeleteSolicitud = "DELETE FROM DetallesPedidosMedida WHERE Detalles_PedidoMedida_id = ?";
            psDeleteSolicitud = conn.prepareStatement(sqlDeleteSolicitud);
            psDeleteSolicitud.setInt(1, idSolicitudMedida);
            psDeleteSolicitud.executeUpdate();

            //Confirmación atómica si todo fue correcto
            conn.commit();
            exito = true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        
            throw e; // Relanzamos el error para que el controlador capture la causa raíz
        } 
        
        finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (psGetSolicitud != null) psGetSolicitud.close(); } catch (Exception e) {}
            try { if (psUnlinkDetalles != null) psUnlinkDetalles.close(); } catch (Exception e) {}
            try { if (psDeleteCotizacion != null) psDeleteCotizacion.close(); } catch (Exception e) {}
            try { if (psDeleteSolicitud != null) psDeleteSolicitud.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        return exito;
    }
}