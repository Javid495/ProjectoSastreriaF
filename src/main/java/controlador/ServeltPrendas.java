package controlador;

import modelo.PrendasDAO;
import com.google.gson.Gson;
import getsSets.Prendas;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Servelt que obtiene y muestra las prendas del catalogo

@WebServlet("/ObtenerPrendas")
public class ServeltPrendas extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse response)
                throws ServletException , IOException {
    
        // Definimos el tipo de contenido y codificación de la respuesta
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            PrendasDAO dao = new PrendasDAO();
            List<Prendas> listaPrendas;
            
            // Detectamos quién solicita los datos
            String rol = solicitud.getParameter("rol");
            
            if ("admin".equals(rol)) {
                // Si viene de la interfaz de administración, incluimos las prendas inactivas
                listaPrendas = dao.listarPrendasAdmin();
            } else {
                // Si es un cliente normal o no se especifica rol, cargamos el catálogo público
                listaPrendas = dao.listarPrendas();
            }
            
            // Usamos Gson para convertir la lista seleccionada en JSON
            Gson gson = new Gson();
            String jsonRespuesta = gson.toJson(listaPrendas);
            
            // Se envía el JSON al navegador (cliente)
            PrintWriter out = response.getWriter();
            out.print(jsonRespuesta);
            out.flush();
            
        } catch (Exception e) {
            // Si se presenta algún error, enviamos un código de estado 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Error en el servlet de prendas: " + e.getMessage());
        }
    }
}
