package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import getsSets.Registro;
import modelo.RegistroDAO;

// Servlet que registra nuevos usuarios
@WebServlet (urlPatterns = {"/Registro"}, loadOnStartup = 1)
public class ServeltRegistro extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // Se recogen los datos del formulario de registro
        String user = request.getParameter("user");
        String contra = request.getParameter("contra");
        String email = request.getParameter("correo");
        String tel = request.getParameter("tel");
        
        //Cambiamos 'int' por 'long' para soportar los 10 dígitos
        long telefonoFinal = 0; 

        if (tel != null && !tel.trim().isEmpty()) {
            try {
                // 🌟 Convertimos usando Long en lugar de Integer
                telefonoFinal = Long.parseLong(tel.trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: El formato del teléfono no es válido: " + tel);
                // Opcional: podrías retornar un error al cliente aquí si el teléfono es obligatorio
            }
        }
        
        // Se llenan los datos en el dto de registro
        Registro prueb = new Registro();
        prueb.setUsuario(user);
        prueb.setContrasena(contra);
        prueb.setEmail(email);  
        prueb.setTelefono(telefonoFinal); 
        
        // Se guardan a través del DAO
        RegistroDAO dao = new RegistroDAO();
        boolean exitoInsert = dao.registrar(prueb, 1);
        
        // Entregar una respuesta al fetch de JS
        if (exitoInsert) {
            response.getWriter().write("ok");
        }
        
        else {
            response.setStatus(500);
            response.getWriter().write("Error al ingresar en la base de datos");
        }
    }
}
