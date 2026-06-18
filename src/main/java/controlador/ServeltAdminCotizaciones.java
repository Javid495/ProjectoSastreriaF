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
import modelo.Prendas; 

@WebServlet("/AdminCotizaciones") // Se define la URL pública para el fetch de JavaScript
public class ServeltAdminCotizaciones extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        
        PrintWriter out = respuesta.getWriter();
        String accion = solicitud.getParameter("accion");
        AdminCotizacionesDAO dao = new AdminCotizacionesDAO();

        // 📊 1. CASO: CONTAR NOTIFICACIONES
        if ("contar".equals(accion)) {
            int cotizar = dao.contarPendientesPorCotizar();
            int nuevos = dao.contarNuevosPedidos();
            
            // Retorna "cantidad" para la animación y los demás contadores por si los usas luego
            out.print("{\"cantidad\":" + cotizar + ", \"pedidosCotizar\":" + cotizar + ", \"nuevosPedidos\":" + nuevos + "}");
          
        // 🪟 2. CASO: LISTAR COTIZACIONES PENDIENTES (A MEDIDA)
        } else if ("listar".equals(accion)) {
            List<String[]> pendientes = dao.listarPedidosPorCotizar();
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < pendientes.size(); i++) {
                String[] item = pendientes.get(i);
                json.append("{");
                json.append("\"id\":").append(item[0]).append(",");
                json.append("\"email\":\"").append(escaparJSON(item[1])).append("\",");
                json.append("\"tipo\":\"").append(escaparJSON(item[2])).append("\",");
                json.append("\"tela\":\"").append(escaparJSON(item[3])).append("\",");
                json.append("\"medidas\":\"").append(escaparJSON(item[4])).append("\",");
                json.append("\"descripcion\":\"").append(escaparJSON(item[5])).append("\",");
                json.append("\"imagen\":\"").append(escaparJSON(item[6])).append("\"");
                json.append("}");
                
                if (i < pendientes.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
            
        // 📉 3. CASO: ALERTAS DE BAJO STOCK (CATÁLOGO)
        } else if ("bajoStock".equals(accion)) {
            // Revisa en la tabla prendas todo lo que sea menor o igual a 5 unidades
            List<Prendas> bajoStock = dao.listarPrendasBajasStock(5);
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < bajoStock.size(); i++) {
                Prendas item = bajoStock.get(i);
                json.append("{");
                json.append("\"id\":").append(item.getId()).append(",");
                json.append("\"nombre\":\"").append(escaparJSON(item.getNombre())).append("\",");
                json.append("\"precio\":").append(item.getValor()).append(","); 
                json.append("\"stock\":").append(item.getStock()).append(",");   
                json.append("\"imagen\":\"").append(escaparJSON(item.getImagen())).append("\""); 
                json.append("}");
                
                if (i < bajoStock.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
        
        out.flush();
    } // <- Aquí se cierra correctamente el método doGet

    // 🛡️ Método auxiliar para blindar tus cadenas de texto en los JSON
    private String escaparJSON(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}



