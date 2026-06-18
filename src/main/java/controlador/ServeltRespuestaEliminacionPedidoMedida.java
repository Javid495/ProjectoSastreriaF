package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dao.ClaseConexion; // Ajusta a tu paquete real

@WebServlet("/ResponderCotizacion")
public class ServeltRespuestaEliminacionPedidoMedida extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String idCotizacionStr = request.getParameter("idPedidoMedida");
        String accion = request.getParameter("accion");
        
        if (idCotizacionStr == null || accion == null || !accion.equals("eliminar")) {
            out.print("{\"success\": false, \"mensaje\": \"Parámetros inválidos.\"}");
            return;
        }
        
        Connection conn = null;
        PreparedStatement psGetSolicitud = null;
        PreparedStatement psUnlinkDetalles = null; // 🆕 Para romper la FK
        PreparedStatement psDeleteCotizacion = null;
        PreparedStatement psDeleteSolicitud = null;
        ResultSet rs = null;
        
        try {
            int idCotizacion = Integer.parseInt(idCotizacionStr);
            conn = ClaseConexion.getConexion(); 
            
            if (conn == null) {
                out.print("{\"success\": false, \"mensaje\": \"Sin conexión a la BD.\"}");
                return;
            }
            
            // 🔑 Iniciamos transacción para que todo pase o nada pase
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
            
            if (idSolicitudMedida == -1) {
                out.print("{\"success\": false, \"mensaje\": \"No se encontró la cotización.\"}");
                conn.rollback();
                return;
            }
            
            // 2. 🆕 PASO CLAVE: Romper el vínculo con DetallesPedidos para evitar el error de FK
            // Como tu diseño permite NULL en esta columna, la liberamos de manera segura.
            String sqlUnlink = "UPDATE DetallesPedidos SET CotizacionPedido_id = NULL WHERE CotizacionPedido_id = ?";
            psUnlinkDetalles = conn.prepareStatement(sqlUnlink);
            psUnlinkDetalles.setInt(1, idCotizacion);
            psUnlinkDetalles.executeUpdate();
            
            // 3. Borrar el hijo (CotizacionPedido)
            String sqlDeleteCotizacion = "DELETE FROM CotizacionPedido WHERE CotizacionPedido_Id = ?";
            psDeleteCotizacion = conn.prepareStatement(sqlDeleteCotizacion);
            psDeleteCotizacion.setInt(1, idCotizacion);
            psDeleteCotizacion.executeUpdate();
            
            // 4. Borrar el padre (DetallesPedidosMedida)
            String sqlDeleteSolicitud = "DELETE FROM DetallesPedidosMedida WHERE Detalles_PedidoMedida_id = ?";
            psDeleteSolicitud = conn.prepareStatement(sqlDeleteSolicitud);
            psDeleteSolicitud.setInt(1, idSolicitudMedida);
            psDeleteSolicitud.executeUpdate();
            
            // 🚀 Si todo salió bien, guardamos los cambios de forma definitiva
            conn.commit();
            out.print("{\"success\": true}");
            
        } catch (NumberFormatException e) {
            out.print("{\"success\": false, \"mensaje\": \"ID con formato inválido.\"}");
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            out.print("{\"success\": false, \"mensaje\": \"Error en el servidor: " + e.getMessage() + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (psGetSolicitud != null) psGetSolicitud.close(); } catch (Exception e) {}
            try { if (psUnlinkDetalles != null) psUnlinkDetalles.close(); } catch (Exception e) {}
            try { if (psDeleteCotizacion != null) psDeleteCotizacion.close(); } catch (Exception e) {}
            try { if (psDeleteSolicitud != null) psDeleteSolicitud.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}