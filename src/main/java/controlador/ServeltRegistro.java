
package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import modelo.Registro;
import dao.RegistroDAO;

@WebServlet (urlPatterns = {"/Registro"}, loadOnStartup = 1)
public class ServeltRegistro extends HttpServlet{
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        //Se recojen los datos del fromulario de registro
        String user = request.getParameter("user");
        String contra = request.getParameter("contra");
        String email = request.getParameter("correo");
        String tel = request.getParameter("tel");
        
        
        int telefonoFinal = 0; // Valor por defecto por si algo falla

        if (tel != null && !tel.trim().isEmpty()) {
            try {
                telefonoFinal = Integer.parseInt(tel.trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: El formato del teléfono no es válido: " + tel);
                
            }
        }
        
        // se llena los datos en el modelo prueba
        Registro prueb = new Registro();
        prueb.setUsuario(user);
        prueb.setContrasena(contra);
        prueb.setEmail(email);  
        prueb.setTelefono(telefonoFinal);
        
        
        //Se guardan en el archivo Dao
        RegistroDAO dao = new RegistroDAO();
        boolean exitoInsert = dao.registrar(prueb, 1);
        
        
        
        //Entregar un respuesta
        if (exitoInsert){
            response.getWriter().write("ok");
        }
        else {
            response.setStatus(500);
            response.getWriter().write("Error al ingresar en la base de datos");
        }
        
        
    }
}
