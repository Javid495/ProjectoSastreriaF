package controlador;

import dao.MostrarPedidosAdminDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

//Servelt encargado de mostrar los detalles de los pedidos del administrador

@WebServlet("/AdminPedidosDetalles")
public class ServeltDetallesPedidosAdminDAO extends HttpServlet {

    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    MostrarPedidosAdminDAO dao = new MostrarPedidosAdminDAO();
    String idParam = request.getParameter("idPedido");

    //Para realizar un consulta individual
    if (idParam != null) {
        int idPedido = Integer.parseInt(idParam);
        List<String[]> listaControl = dao.listarPedidosParaAdmin();
        String tipo = "Catálogo"; 
        
        for (String[] p : listaControl) {
            if (Integer.parseInt(p[0]) == idPedido) {
                tipo = p[7]; 
                break;
            }
        }

        StringBuilder json = new StringBuilder("{");
        if ("A Medida".equalsIgnoreCase(tipo)) {
            Map<String, Object> data = dao.obtenerDetalleAMedida(idPedido);
            
            // Validar que existan datos antes de armar el JSON para evitar NullPointerException
            if(data != null && !data.isEmpty()) {
                json.append("\"tipo\":\"A Medida\",")
                    .append("\"fecha\":\"").append(data.get("fecha") != null ? data.get("fecha") : "").append("\",")
                    .append("\"email\":\"").append(data.get("email") != null ? data.get("email") : "").append("\",")
                    .append("\"prenda\":\"").append(data.get("prenda") != null ? data.get("prenda") : "").append("\",")
                    .append("\"tela\":\"").append(data.get("tela") != null ? data.get("tela") : "").append("\",")
                    .append("\"medidas\":\"").append(data.get("medidas") != null ? data.get("medidas") : "").append("\",")
                    .append("\"descripcion\":\"").append(data.get("descripcion") != null ? data.get("descripcion") : "").append("\",")
                    .append("\"imagen\":\"").append(data.get("imagen") != null ? data.get("imagen") : "").append("\",")
                    .append("\"total\":").append(data.get("total") != null ? data.get("total") : 0);
            }
        } 
        
        else {
            Map<String, Object> data = dao.obtenerDetalleCatalogo(idPedido);
            json.append("\"tipo\":\"Catalogo\",")
                .append("\"fecha\":\"").append(data.get("fecha") != null ? data.get("fecha") : "").append("\",")
                .append("\"email\":\"").append(data.get("email") != null ? data.get("email") : "").append("\",")
                .append("\"total\":").append(data.get("total") != null ? data.get("total") : 0).append(",")
                .append("\"prendas\":[");
            
            List<Map<String, String>> prendas = (List<Map<String, String>>) data.get("prendas");
            if (prendas != null) {
                for (int i = 0; i < prendas.size(); i++) {
                    Map<String, String> pr = prendas.get(i);
                    json.append("{")
                        .append("\"nombre\":\"").append(pr.get("nombre")).append("\",")
                        .append("\"precio\":\"").append(pr.get("precio")).append("\",")
                        .append("\"talla\":\"").append(pr.get("talla")).append("\",")
                        .append("\"imagen\":\"").append(pr.get("imagen")).append("\"")
                        .append("}");
                    if (i < prendas.size() - 1) json.append(",");
                }
            }
            json.append("]");
        }
        
        json.append("}");
        response.getWriter().write(json.toString());
        return;
    }

    //Flujo que menja el listado de todos lo pedidos
    List<String[]> pedidos = dao.listarPedidosParaAdmin();
    StringBuilder jsonList = new StringBuilder("[");
    for (int i = 0; i < pedidos.size(); i++) {
        String[] p = pedidos.get(i);
        jsonList.append("{")
            .append("\"id\":\"").append(p[0]).append("\",")
            .append("\"fecha\":\"").append(p[1]).append("\",")
            .append("\"estado\":\"").append(p[2]).append("\",")
            .append("\"total\":\"").append(p[3]).append("\",")  // 🔥 CORREGIDO: p[3] es total
            .append("\"medidas\":\"").append(p[4]).append("\",")
            .append("\"email\":\"").append(p[5]).append("\",")
            .append("\"tipo\":\"").append(p[7]).append("\"")    // 🔥 CORREGIDO: p[7] es tipo real
            .append("}");
        if (i < pedidos.size() - 1) jsonList.append(",");
    }
    jsonList.append("]");
    response.getWriter().write(jsonList.toString());
}

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
