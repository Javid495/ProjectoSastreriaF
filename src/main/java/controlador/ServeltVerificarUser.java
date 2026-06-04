
package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import modelo.IniciarSesion;

@WebServlet("/VerificarSesion")
public class ServeltVerificarUser extends HttpServlet {
 
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
    
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        //Se obtiene la sesion actual sin crear o generar una nueva
        HttpSession session = solicitud.getSession(false);
        
        if (session != null && session.getAttribute("PerfilUsuario") != null) {
            
            //Se Recupera el objecto guardado en el login
            IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
            
            int idUsuario = usuarioLog.getId();
            String nombreUsuario = usuarioLog.getUsuario();
            String userImagen = usuarioLog.getImagen();
            int rolUsuario = usuarioLog.getRolUsuario();
            
            //Se construye un respuesta en el json
            if (userImagen != null) {
                // Reemplaza las barras invertidas de Windows (\) por barras normales web (/)
                userImagen = userImagen.replace("\\", "/");
            } else {
                // Ruta de respaldo por si el usuario no tiene ninguna imagen registrada
                userImagen = "images/Perfil/Ellipse 14.png";
            }
            
            // Se construye la respuesta en el JSON con la ruta ya limpia
            out.print("{\"logeado\": true, \"id\": " + idUsuario + ", \"nombre\": \"" + nombreUsuario + "\", \"imagen\": \"" + userImagen + "\", \"rol\": " + rolUsuario + "}");    
        }
        
        else{
            //Si no hay una sesion activa
            out.print("{\"logeado\":false}");
        }
        
        out.flush();
    }
}
