
package controlador;

import dao.PrendasDAO;
import com.google.gson.Gson;
import modelo.Prendas;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Servelt quien obtiene las prendas del catalogo y las imprime
//Al cliente

//Realizamos o creamo nuestra referencia a js
@WebServlet("/ObtenerPrendas")

public class ServeltPrendas extends HttpServlet{
    
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse response)
                throws ServletException , IOException {
    
        //Definimos el tipo de contenido que la respuesta
        response.setContentType("application/json");
        
        // Definimos el lenguaje estandar del contenido
        response.setCharacterEncoding("UTF-8");
        
        try{
            //Instanciamos a nuestro archivo prendasDAO y obtenemos la lista de prendsas
            PrendasDAO dao = new PrendasDAO();
            List<Prendas> listaPrendas = dao.listarPrendas();
            
            //Usamos el Gson para convertir la lista en un json
            Gson gson = new Gson();
            String jsonRespuesta = gson.toJson(listaPrendas);
            
            //Se envia el json al cliente (el navegador)
            PrintWriter out = response.getWriter();
            out.print(jsonRespuesta);
            out.flush();
            
            
        }
        catch (Exception e){
            //Si se llega a presentar algun error entonces, enviamos un error 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Error en el servelt de prendas: " + e.getMessage());
        
        }
    }
}
