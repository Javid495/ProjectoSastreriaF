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
import modelo.PedidosClienteDAO;
import Dtos.IniciarSesion;
import Dtos.Pedidos;

//Servelt encargado de solicitar y mostrar los pedidos de los usuarios.

@WebServlet("/ObtenerPedidos")
public class ServeltObtenerPedidosUser extends HttpServlet {
    
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
    
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        HttpSession session = solicitud.getSession(false);
        
        if (session != null && session.getAttribute("PerfilUsuario") != null) {
            IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
            int idUsuario = usuarioLog.getId(); 
            
            PedidosClienteDAO dao = new PedidosClienteDAO();
            List<Pedidos> misPedidos = dao.obtenerPedidosUsuario(idUsuario);
            
            StringBuilder json = new StringBuilder();
            json.append("{\"logeado\": true, \"pedidos\":[");
            
            for (int i = 0; i < misPedidos.size(); i++) {
                Pedidos p = misPedidos.get(i);
                
                // Abrimos el objeto del pedido con sus datos básicos
                json.append(String.format("{\"id\": %d, \"fecha\": \"%s\", \"tipo\": \"%s\", \"estado\": \"%s\"",
                        p.getIdPedido(), p.getFechaInicio(), p.getTipoCompra(), p.getEstadoPedido()));
                
                // 🔍 ANALIZAMOS EL TIPO DE COMPRA (Validación segura ignorando mayúsculas)
                String tipoCompra = p.getTipoCompra() != null ? p.getTipoCompra().toLowerCase() : "";
                
                if (tipoCompra.contains("medida")) {
                    
                    // Caso A: Es un pedido hecho a medida desde cero
                    String[] deMedida = dao.obtenerDetallesPedidoMedida(p.getIdPedido());
                    
                    if (deMedida != null) {
                        // Limpieza básica de comentarios para evitar que rompan el JSON si tienen comillas dobles
                        String comentario = deMedida[3] != null ? deMedida[3].replace("\"", "\\\"") : "";
                        
                        json.append(String.format(", \"detalleMedida\": {\"tipoPrenda\": \"%s\", \"tela\": \"%s\", \"valor\": \"%s\", \"comentario\": \"%s\"}",
                                deMedida[0], deMedida[1], deMedida[2], comentario));
                    } 
                    
                    else {
                        json.append(", \"detalleMedida\": null");
                    }
                    
                    // Enviamos un array de productos vacío para mantener consistencia en el Frontend
                    json.append(", \"productos\": []");
                    
                } 
                
                else {
                    // Caso B: Es un pedido de prendas del catálogo (Carrito estándar)
                    List<String[]> productos = dao.obtenerProductosPorPedido(p.getIdPedido());
                    
                    json.append(", \"productos\": [");
                    for (int j = 0; j < productos.size(); j++) {
                        String[] prod = productos.get(j);
                        String imgUrl = prod[4] != null ? prod[4] : "";
                        
                        json.append(String.format("{\"nombre\": \"%s\", \"precio\": \"%s\", \"total\": \"%s\", \"cantidad\": %s, \"imagen\": \"%s\"}",
                                prod[0], prod[1], prod[2], prod[3], imgUrl));
                        
                        if (j < productos.size() - 1) json.append(",");
                    }
                    json.append("]");
                    // Enviamos el nodo de medida como nulo
                    json.append(", \"detalleMedida\": null");
                }
                
                // Cerramos el objeto del pedido individual
                json.append("}");
                
                if (i < misPedidos.size() - 1) json.append(",");
            }
            
            json.append("]}");
            out.print(json.toString());
        }
        
        else {
            out.print("{\"logeado\": false, \"mensaje\": \"Debes iniciar sesión para ver tus pedidos.\"}");
        }
        
        out.flush();
    }
}