package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;

import Dtos.Prendas;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.PopularesDAO;

// Servlet encargado de proveer las prendas más populares para la página de inicio
@WebServlet("/ObtenerPopularesInicio")
public class ServeltPopulares extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configuramos la respuesta para que el navegador sepa que recibe un JSON en UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            
            // 1. Instanciamos el DAO de populares
            PopularesDAO popularesDAO = new PopularesDAO();
            
            // 2. Solicitamos el top 10 de prendas (puedes cambiar el número si deseas más o menos)
            List<Prendas> listaPopulares = popularesDAO.obtenerTopPopulares(10);
            
            // 3. Convertimos la lista de Java a una cadena JSON utilizando Gson
            Gson gson = new Gson();
            String json = gson.toJson(listaPopulares);
            
            // 4. Imprimimos el JSON hacia el Frontend
            out.print(json);
            
        } 
        
        catch (Exception e) {
            e.printStackTrace();
            // En caso de error interno, respondemos con un código 500
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar las prendas populares.");
        }
    }
}
