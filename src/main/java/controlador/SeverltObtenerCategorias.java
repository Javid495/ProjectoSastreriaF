
package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import dao.PrendasDAO;

@WebServlet("/ObtenerCategorias")
public class SeverltObtenerCategorias extends HttpServlet {
    
    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            PrendasDAO dao = new PrendasDAO();
            List<Map<String, String>> listaCategorias = dao.listarCategorias();
        
            // Construimos el JSON de objetos: [{"id":"1","nombre":"Saco"}]
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < listaCategorias.size(); i++) {
                Map<String, String> cat = listaCategorias.get(i);
                json.append("{");
                json.append("\"id\":\"").append(cat.get("id")).append("\",");
                json.append("\"nombre\":\"").append(cat.get("nombre")).append("\"");
                json.append("}");
                
                if (i < listaCategorias.size() - 1) {
                    json.append(",");
                }
        }
            json.append("]");
        
            response.getWriter().write(json.toString());
            
        } 
        catch (Exception e) {
            System.out.println("Error al obtener categorías: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("[]");
        }
        
    }
}
