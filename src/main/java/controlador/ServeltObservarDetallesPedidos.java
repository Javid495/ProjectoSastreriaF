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


//Servelt encargado de mostrar los detalles de los pedidos Sean del catalogo o sean
//de pedidos a medida
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
            // 🌟 CAPTURAMOS EL TIPO DE PEDIDO: "Catálogo" o "A Medida" enviado desde tu JavaScript
            String tipoPedido = solicitud.getParameter("tipoPedido"); 
            
            if (tipoPedido == null) {
                tipoPedido = "Catálogo"; // Salvaguarda por defecto
            }

            PedidosClienteDAO dao = new PedidosClienteDAO();
            StringBuilder json = new StringBuilder();

            // 🔀 BIFURCACIÓN ESTRATÉGICA
            if ("A Medida".equalsIgnoreCase(tipoPedido)) {
                
                // =================================================================
                // FLUJO NUEVO: PROCESAR DESGLOSE DE PRENDA A MEDIDA
                // =================================================================
                String[] itemMedida = dao.obtenerDetallesPedidoMedida(idPedido);

                json.append("["); // Abrimos el array igual que en catálogo
                if (itemMedida != null) {
                    // Mimetizamos las llaves idénticas a catálogo para engañar al Frontend con éxito
                    json.append("{");
                    json.append("\"nombre\":\"").append(itemMedida[0]).append(" (A Medida - Tela: ").append(itemMedida[1]).append(")\",");
                    json.append("\"precio\":").append(itemMedida[2]).append(",");
                    json.append("\"totalLineal\":").append(itemMedida[2]).append(",");
                    json.append("\"cantidad\":1,"); // Un pedido a medida es una pieza única mapeada
                    
                    // Si el sastre dejó notas, las inyectamos de forma segura limpiando comillas
                    String notaLimpia = itemMedida[3] != null ? itemMedida[3].replace("\"", "'") : "";
                    json.append("\"imagen\":\"\","); // Las prendas desde cero no tienen una foto preestablecida
                    json.append("\"nota\":\"").append(notaLimpia).append("\""); 
                    json.append("}");
                }
                json.append("]");

            } else {
                
                // =================================================================
                // 🛒 FLUJO ORIGINAL: PROCESAR PRODUCTOS DE CATÁLOGO (Tu código intacto)
                // =================================================================
                List<String[]> detalles = dao.obtenerProductosPorPedido(idPedido);

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
            }

            // Enviamos el resultado unificado
            out.print(json.toString());
            
        } 
        catch (Exception e) {
            // Ante cualquier fallo de conversión numérico o nulo, responde estructura vacía segura
            out.print("[]");
        }
        out.flush();
    }
}