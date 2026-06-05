
package controlador;

import dao.MostrarPedidosAdminDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

//Servelt encargado de listar y cambiar el estado de los pedidos


@WebServlet("/AdminPedidosController")
public class ServeltAdminPedidos extends HttpServlet {

    // GET: Devuelve la lista completa de pedidos en formato JSON
    @Override
    
    //Peparamos uun metodo get de la respues
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        MostrarPedidosAdminDAO dao = new MostrarPedidosAdminDAO();
        List<String[]> pedidos = dao.listarPedidosParaAdmin();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < pedidos.size(); i++) {
            String[] p = pedidos.get(i);
            json.append("{")
                .append("\"id\":\"").append(p[0]).append("\",")
                .append("\"fecha\":\"").append(p[1]).append("\",")
                .append("\"estado\":\"").append(p[2]).append("\",")
                .append("\"tipo\":\"").append(p[3]).append("\",")
                .append("\"medidas\":\"").append(p[4]).append("\"")
                .append("}");
            if (i < pedidos.size() - 1) json.append(",");
        }
        json.append("]");
        response.getWriter().write(json.toString());
    }

    //Cambia el estado de un pedido desde el selector de la Card
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");

        int idPedido = Integer.parseInt(request.getParameter("idPedido"));
        String nuevoEstado = request.getParameter("estado");

        MostrarPedidosAdminDAO dao = new MostrarPedidosAdminDAO();
        boolean exito = dao.actualizarEstadoPedido(idPedido, nuevoEstado);

        response.getWriter().write("{\"success\": " + exito + "}");
    }
}