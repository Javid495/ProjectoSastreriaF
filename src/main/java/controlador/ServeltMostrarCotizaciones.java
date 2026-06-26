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
import modelo.PedidosMedidaDao;
import getsSets.IniciarSesion;

//Servelt encargado de mostrar las cotizaciones por el administrador

@WebServlet("/MisCotizaciones")
public class ServeltMostrarCotizaciones extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        HttpSession session = solicitud.getSession(false);
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"mensaje\": \"Inicie sesión para ver sus cotizaciones.\"}");
            return;
        }

        // Extraemos el ID del usuario en sesión
        IniciarSesion user = (IniciarSesion) session.getAttribute("PerfilUsuario");
        PedidosMedidaDao dao = new PedidosMedidaDao();
        List<String[]> cotizaciones = dao.listarCotizacionesUsuario(user.getId());
        
        // Construcción manual del JSON (Mapeando el nuevo índice de la cotización)
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < cotizaciones.size(); i++) {
            String[] item = cotizaciones.get(i);
            json.append("{");
            json.append("\"id\":").append(item[0]).append(","); // ID de la Solicitud de Medida
            json.append("\"tipo\":\"").append(item[1]).append("\",");
            json.append("\"tela\":\"").append(item[2]).append("\",");
            json.append("\"medidas\":\"").append(item[3]).append("\",");
            json.append("\"descripcion\":\"").append(item[4]).append("\",");
            json.append("\"imagen\":\"").append(item[5]).append("\",");
            json.append("\"precio\":").append(item[6]).append(",");
            json.append("\"comentario\":\"").append(item[7]).append("\",");
            json.append("\"fechaLimite\":\"").append(item[8]).append("\","); // 🌟 Cambiado a coma
            
            //Inyectamos el ID real de la cotización (índice 9) para el JS
            json.append("\"CotizacionPedido_Id\":").append(item[9]); 
            
            json.append("}");
            if (i < cotizaciones.size() - 1) json.append(",");
        }
        json.append("]");
        
        out.print(json.toString());
        out.flush();
    }
}