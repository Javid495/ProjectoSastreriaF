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
import modelo.Prendas; // ¡No olvides importar tu modelo!

@WebServlet("/AdminCotizaciones")
public class ServeltAdminCotizaciones extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        String accion = solicitud.getParameter("accion");
        AdminCotizacionesDAO dao = new AdminCotizacionesDAO();

        if ("contar".equals(accion)) {
            int cotizar = dao.contarPendientesPorCotizar();
            int nuevos = dao.contarNuevosPedidos();
            
            // Retornamos un único objeto JSON con ambas propiedades
            out.print("{\"pedidosCotizar\":" + cotizar + ", \"nuevosPedidos\":" + nuevos + "}");
            
        } else if ("listar".equals(accion)) { // Se eliminó la doble llave { { errónea
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
            
        } else if ("bajoStock".equals(accion)) {
            // ARREGLADO: Ahora mapea correctamente a List<Prendas> y enviamos el límite de unidades (5)
            List<Prendas> bajoStock = dao.listarPrendasBajasStock(5);
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < bajoStock.size(); i++) {
                Prendas item = bajoStock.get(i);
                json.append("{");
                json.append("\"id\":").append(item.getId()).append(",");
                json.append("\"nombre\":\"").append(item.getNombre()).append("\",");
                json.append("\"precio\":").append(item.getValor()).append(","); // Mapeado de getValor()
                json.append("\"stock\":").append(item.getStock()).append(",");   // Mapeado de getStock()
                json.append("\"imagen\":\"").append(item.getImagen()).append("\""); // Mapeado de getImagen()
                json.append("}");
                if (i < bajoStock.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
        out.flush();
    }
}
