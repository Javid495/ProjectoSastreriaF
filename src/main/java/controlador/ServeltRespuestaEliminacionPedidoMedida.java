package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.CotizacionRespuestaDAO;

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
        
        // Validación básica de parámetros de entrada
        if (idCotizacionStr == null || accion == null || !accion.equals("eliminar")) {
            out.print("{\"success\": false, \"mensaje\": \"Parámetros inválidos o acción no permitida.\"}");
            return;
        }
        
        try {
            int idCotizacion = Integer.parseInt(idCotizacionStr);
            
            // Instanciamos el DAO y delegamos la operación
            CotizacionRespuestaDAO cotizacionDAO = new CotizacionRespuestaDAO();
            boolean eliminadoConExito = cotizacionDAO.eliminarCotizacionYSolicitud(idCotizacion);
            
            if (eliminadoConExito) {
                out.print("{\"success\": true}");
            } else {
                out.print("{\"success\": false, \"mensaje\": \"No se encontró la cotización especificada.\"}");
            }
            
        } 
        
        catch (NumberFormatException e) {
            out.print("{\"success\": false, \"mensaje\": \"El ID enviado no tiene un formato válido.\"}");
        } 
        
        catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"mensaje\": \"Error de persistencia: " + e.getMessage() + "\"}");
        } 
        
        catch (Exception e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"mensaje\": \"Error interno en el servidor: " + e.getMessage() + "\"}");
        }
    }
}