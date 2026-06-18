package controlador;

import com.google.gson.Gson;
import dao.HistorialPagosDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Dtos.ReporteCajaDTO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import dao.ClaseConexion;

// Servlet que obtiene y genera el historial de los pagos

@WebServlet("/ObtenerHistorialPagos")
public class ServeltObtenerHistorialPagos extends HttpServlet {
    private final HistorialPagosDAO pagos = new HistorialPagosDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try (Connection conn = ClaseConexion.getConexion(); PrintWriter out = response.getWriter()) {
            
            ReporteCajaDTO reporte = pagos.obtenerReporteAdministrativo(conn);
            
            String jsonRespuesta = new Gson().toJson(reporte);
            out.print(jsonRespuesta);
            out.flush();
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}