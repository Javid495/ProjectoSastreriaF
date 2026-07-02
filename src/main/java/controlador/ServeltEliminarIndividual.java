package controlador;

import modelo.EliminarPrendasDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//Servelt encargado de eliminar las prendas uno a uno 
//En el catalogo Admin

@WebServlet("/EliminarPrendaServlet")
public class ServeltEliminarIndividual extends HttpServlet {

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Capturamos el parámetro 'id' enviado en la URL del fetch
            String idParam = request.getParameter("id");
            
            if (idParam == null || idParam.trim().isEmpty()) {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"ID de prenda ausente.\"}");
                return;
            }

            int idPrenda = Integer.parseInt(idParam);

            // Invocamos la eliminación en cascada en el DAO
            EliminarPrendasDAO dao = new EliminarPrendasDAO();
            boolean eliminadoExitoso = dao.eliminarPrendaCompleta(idPrenda);

            if (eliminadoExitoso) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            } 
            
            else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se pudo eliminar el registro en la base de datos.\"}");
            }

        } 
        
        catch (NumberFormatException e) {
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"El ID provisto no es un número válido.\"}");
            System.out.print(e);
            
        } 
        
        catch (Exception e) {
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error crítico: " + e.getMessage() + "\"}");
        }
    }
}