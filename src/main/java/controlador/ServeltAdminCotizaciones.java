package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.AdminCotizacionesDAO;

//Creamos nuestra referencia con el frontend
@WebServlet("/AdminCotizaciones")
public class ServeltAdminCotizaciones extends HttpServlet {

    @Override
    //Hacemos nuestra peticiones junto con la peticion de lc cliente, y la respuesta 
    //Que vamos a retornar
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        String accion = solicitud.getParameter("accion");
        AdminCotizacionesDAO dao = new AdminCotizacionesDAO();

        if ("contar".equals(accion)) {
            int cantidad = dao.contarPendientesPorCotizar();
            out.print("{\"cantidad\":" + cantidad + "}");
            
        } else if ("listar".equals(accion)) {
            List<String[]> pendientes = dao.listarPedidosPorCotizar();
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < pendientes.size(); i++) {
                String[] item = pendientes.get(i);
                json.append("{");
                json.append("\"id\":").append(item[0]).append(",");
                json.append("\"email\":\"").append(item[1]).append("\",");
                json.append("\"tipo\":\"").append(item[2]).append("\",");
                json.append("\"tela\":\"").append(item[3]).append("\",");
                json.append("\"medidas\":\"").append(item[4]).append("\",");
                json.append("\"descripcion\":\"").append(item[5]).append("\",");
                json.append("\"imagen\":\"").append(item[6]).append("\"");
                json.append("}");
                if (i < pendientes.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
        out.flush();
    }
}
