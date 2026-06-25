
package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Se define la clase que se encarga del acceso de datos para respuestas de cotizaciones
public class CotizacionRespuestaDAO {

    /**
     * Rompe vínculos y elimina una cotización junto con su solicitud a medida
     * usando un bloque transaccional atómico.
     */
    
    // Y el metodo que hace referencia a eliminarCotizacionYSolicitud retornara un booleano (true o false)
    // y recibe el id de la cotizacion a eliminar y se prepara porsi llega a ver un error en la eliminaicon
    public boolean eliminarCotizacionYSolicitud(int idCotizacion) throws SQLException {
        
        //Se declaran las herramienta a usar esto se declara para que en el finally se pueda cerrar correctamente
        
        //Conexion con la base de datos
        Connection conn = null;
        
        PreparedStatement psGetSolicitud = null;
        PreparedStatement psUnlinkDetalles = null;
        PreparedStatement psDeleteCotizacion = null;
        PreparedStatement psDeleteSolicitud = null;
        ResultSet rs = null;
        
        //Resultado de la eliminacion
        boolean exito = false;

        try {
            
            //Establecemos conexion 
            conn = ClaseConexion.getConexion();
            
            //En caso de que se presente algun error en la conexion
            if (conn == null) {
                return false;
            }

            // Transacción controlada en caso de que se presente algun inconveniente
            conn.setAutoCommit(false);

            // 1. Obtener el ID de la solicitud original
            String sqlFindSolicitud = "SELECT DetallesPedidosMedida_id FROM CotizacionPedido WHERE CotizacionPedido_Id = ?";
            
            //Preparamos el codigo
            psGetSolicitud = conn.prepareStatement(sqlFindSolicitud);
            
            //Asignamos el id de la cotizacion 
            psGetSolicitud.setInt(1, idCotizacion);
            
            //Ejecutamos el codigo
            rs = psGetSolicitud.executeQuery();

            //Inicamos una variable controlable
            int idSolicitudMedida = -1;
            
            //Obtenemos el id del Pedido a medida
            if (rs.next()) {
                idSolicitudMedida = rs.getInt("DetallesPedidosMedida_id");
            }

            // Si la cotización no existe, cancelamos todo de inmediato
            if (idSolicitudMedida == -1) {
                
                //Se devuelve al checkpoint establecido anteriormente
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
            
            //Finalmente cerramos la conexions que se establecieron
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