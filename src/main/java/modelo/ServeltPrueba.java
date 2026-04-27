
package modelo;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet ("/Prueba")
public class ServeltPrueba extends HttpServlet{
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        //Se recojen los datos del fromulario de registro
        String email = request.getParameter("correo");
        String user = request.getParameter("user");
        String contra = request.getParameter("contra");
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
        Prueba prueb = new Prueba();
        prueb.setEmail(email);
        prueb.setUsuario(user);
        prueb.setContrasena(contra);
        prueb.setTelefono(telefonoFinal);
        
        
        //Se guardan en el archivo Dao
        PruebaDAO dao = new PruebaDAO();
        boolean exitoInsert = dao.registrar(prueb);
        
        
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
