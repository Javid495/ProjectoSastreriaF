package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.PedidosClienteDAO;

@WebServlet("/ObtenerDetallePedido")
public class ServeltObservarDetallesPedidos extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {

        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();

        try {
            int idPedido = Integer.parseInt(solicitud.getParameter("idPedido"));
            
            // Reutilizamos tu clase DAO existente
            PedidosClienteDAO dao = new PedidosClienteDAO();
            List<String[]> detalles = dao.obtenerProductosPorPedido(idPedido);

            // Armamos el JSON estructural de respuesta
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < detalles.size(); i++) {
                String[] item = detalles.get(i);
    
                json.append("{");
                json.append("\"nombre\":\"").append(item[0]).append("\",");
                json.append("\"precio\":").append(item[1]).append(",");
                json.append("\"totalLineal\":").append(item[2]).append(",");
                json.append("\"cantidad\":").append(item[3]).append(",");
                json.append("\"imagen\":\"").append(item[4] != null ? item[4] : "").append("\"");
                json.append("}");
    
                if (i < detalles.size() - 1) json.append(",");
            }
            json.append("]");

            out.print(json.toString());
        } 
        catch (Exception e) {
            out.print("[]");
        }
        out.flush();
    }
}