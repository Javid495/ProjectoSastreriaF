package controlador;

import modelo.PrendasDAO;
import modelo.PopularesDAO;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Servelt encargado de manejar los detalles de los productos

@WebServlet("/ObtenerProductosDetalle")
public class ServeltObtenerDetallesProd extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idParam = request.getParameter("id");
        
        try (PrintWriter out = response.getWriter()) {
        
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el id del producto");
                return;
            }
            
            int id = Integer.parseInt(idParam);
            
            PrendasDAO dao = new PrendasDAO();
            // Llamamos al nuevo método que junta el producto, sus imágenes y todas sus tallas
            Map<String, Object> prendaDetalle = dao.obtenerDetallesPrendaConVariantes(id);
            
            if (prendaDetalle != null) {
                Gson gson = new Gson();
                String json = gson.toJson(prendaDetalle);
                out.print(json);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado o sin existencias");
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID no válido");
        }
    }
}

