
package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.PedidosClienteDAO;
import modelo.IniciarSesion;
import modelo.Pedidos;

//Servelt para mostrar los pedidos por usuario (Cliente)

//Creamos nuestra referencia al js
@WebServlet("/ObtenerPedidos")
public class ServeltObtenerPedidosUser extends HttpServlet {
    
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException{
    
        //Para poder convertir los datos que me retorna mysql a json
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        HttpSession session = solicitud.getSession(false);
        
        //Valida si hay un usuario  activo
        if (session != null && session.getAttribute("PerfilUsuario") != null){
            IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
            int idUsuario = usuarioLog.getId(); //id de la tabla de usuarios
            
            PedidosClienteDAO dao = new PedidosClienteDAO();
            List <Pedidos> misPedidos = dao.obtenerPedidosUsuario(idUsuario);
            
            //Construir el json
            StringBuilder json = new StringBuilder();
            json.append("{\"logeado\": true, \"pedidos\":[");
            for (int i = 0; i < misPedidos.size(); i++){
                
                Pedidos p = misPedidos.get(i);
                json.append(String.format("{\"id\": %d, \"fecha\": \"%s\", \"tipo\": \"%s\", \"estado\": \"%s\"}",
                        p.getIdPedido(), p.getFechaInicio(), p.getTipoCompra(), p.getEstadoPedido()));
                
                if(i < misPedidos.size() - 1) json.append(",");
            }
            
            json.append("]}");
            
            out.print(json.toString());
        }
     
        else{
            out.print("{\"logeado\": false, \"mensaje\": \"Debes iniciar sesión para ver tus pedidos.\"}");
        }
        
        out.flush();
    }
            
}