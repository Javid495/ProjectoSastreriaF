package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import java.io.IOException;

//Servelt Encargado de CerrarSesion o destruir la sesion activa

@WebServlet("/CerrarSesion")
public class ServeltCerrarSesion extends HttpServlet {
    
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        // Buscamos la sesión activa
        HttpSession session = solicitud.getSession(false);
        
        if (session != null) {
            session.removeAttribute("PerfilUsuario"); // Remueve el atributo
            session.invalidate(); // Destruye la sesión por completo de la memoria de Tomcat
        }
        
        // Respondemos con un estado 200 OK
        respuesta.setStatus(HttpServletResponse.SC_OK);
    }
}