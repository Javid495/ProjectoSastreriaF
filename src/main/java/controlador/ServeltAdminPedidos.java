package controlador;

import dao.MostrarPedidosAdminDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// Servlet encargado de listar y cambiar el estado de los pedidos
@WebServlet("/AdminPedidosController")
public class ServeltAdminPedidos extends HttpServlet {

    // GET: Devuelve la lista completa de pedidos en formato JSON para el Admin
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            MostrarPedidosAdminDAO dao = new MostrarPedidosAdminDAO();
            List<String[]> pedidos = dao.listarPedidosParaAdmin();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < pedidos.size(); i++) {
                String[] p = pedidos.get(i);
                
                // Sincronización perfecta con el orden de las columnas del DAO
                json.append("{")
                    .append("\"id\":\"").append(p[0]).append("\",")
                    .append("\"fecha\":\"").append(p[1]).append("\",")
                    .append("\"estado\":\"").append(p[2]).append("\",")
                    .append("\"total\":\"").append(p[3]).append("\",") 
                    .append("\"medidas\":\"").append(p[4]).append("\",") // 🔥 CORREGIDO: Se añade la coma faltante
                    .append("\"usuario\":\"").append(p[5]).append("\",") // Inyección del nombre del usuario
                    .append("\"prenda\":\"").append(p[6]).append("\"")    // Inyección del tipo de prenda
                    .append("}");
                
                if (i < pedidos.size() - 1) json.append(",");
            }
            json.append("]");
            out.write(json.toString());
            
        } catch (ArrayIndexOutOfBoundsException e) {
            // Captura si el array del DAO se quedó corto en columnas (evita el Error 500 de Tomcat)
            out.write("{\"status\": 500, \"success\": false, \"mensaje\": \"Error de índices: Tu consulta SQL en el DAO aún no devuelve los campos de usuario y prenda (p[5] y p[6]).\"}");
        } catch (Exception e) {
            out.write("{\"status\": 500, \"success\": false, \"mensaje\": \"Error inesperado en el servidor: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }

    // POST: Cambia el estado de un pedido desde el selector de la Card/Tabla
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 🛡️ Guardián de seguridad: Solo administradores logueados pueden alterar estados
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            out.print("{\"success\": false, \"mensaje\": \"Acceso denegado. Inicie sesión.\"}");
            return;
        }

        try {
            int idPedido = Integer.parseInt(request.getParameter("idPedido"));
            String nuevoEstado = request.getParameter("estado");

            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                out.print("{\"success\": false, \"mensaje\": \"El estado no puede estar vacío.\"}");
                return;
            }

            MostrarPedidosAdminDAO dao = new MostrarPedidosAdminDAO();
            boolean exito = dao.actualizarEstadoPedido(idPedido, nuevoEstado.toLowerCase().trim());

            out.print("{\"success\": " + exito + "}");
            
        } catch (NumberFormatException e) {
            out.print("{\"success\": false, \"mensaje\": \"ID de pedido inválido.\"}");
        } catch (Exception e) {
            out.print("{\"success\": false, \"mensaje\": \"Error en el servidor: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}