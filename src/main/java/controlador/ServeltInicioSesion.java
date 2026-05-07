
package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import java.io.IOException;
import modelo.IniciarSesion;
import dao.LoginDAO;

    
    @WebServlet("/Login")
    
    public class ServeltInicioSesion extends HttpServlet{
        
        protected void doPost(HttpServletRequest Solicitud, HttpServletResponse respuesta)
                throws ServletException, IOException{
            
            try{
            String UsuarioOrEmail = Solicitud.getParameter("txtUser");
                String Contrasena = Solicitud.getParameter("txtContra");
                
                System.out.println("Se obtuvieron los datos");
                
                //Nos comunicamos con el dao
                LoginDAO ver = new LoginDAO();
                
                //Enviamos los datos que ingreso el usario para que el dao realice las verificaciones
                IniciarSesion userAcceses = ver.validarUsuario(UsuarioOrEmail, Contrasena);
                
                if (userAcceses != null){
                
                    //En caso de que la comprobacion retorne una respuesta positiva
                    //Se crea la sesion del usuario
                    HttpSession session = Solicitud.getSession();
                    session.setAttribute(   "PerfilUsuario", userAcceses);
                    
                    //Se retorna una respuesta al js para informar que todo este bien
                    
                    respuesta.getWriter().write("Hecho");
                    
                }
                else{
                
                    //Si se presenta algun error en la identificacion
                    respuesta.getWriter().write("Hubo algun inconveniente durante la identificacion");
                }
            }
            catch(Exception a){
                System.out.println("Hey hay algun error al momento de enviar lo datos al servelt");
            }
        
                
        }
    
    }
    