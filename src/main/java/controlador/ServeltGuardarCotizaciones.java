package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import modelo.AdminCotizacionesDAO;


//Servelt Encargado de guardar la cotizacion del admin
@WebServlet("/GuardarCotizacion")
public class ServeltGuardarCotizaciones extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {

        // Configuramos la cabecera para responder en formato JSON estructurado
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();

        try {
            // Extraemos los parámetros enviados mediante URLSearchParams por el Frontend
            String idStr = solicitud.getParameter("idPedidoMedida");
            String precioStr = solicitud.getParameter("precio");
            String fechaLimite = solicitud.getParameter("fechaLimite"); // Captura el "YYYY-MM-DD"
            String comentario = solicitud.getParameter("comentario");

            //Validación básica de nulidad para evitar fallos de procesamiento
            if (idStr == null || precioStr == null || fechaLimite == null) {
                out.print("{\"success\": false, \"mensaje\": \"Faltan parámetros obligatorios en la solicitud.\"}");
                return;
            }

            // Parseamos los tipos de datos correspondientes
            int idPedidoMedida = Integer.parseInt(idStr);
            double precio = Double.parseDouble(precioStr);

            // Instanciamos el DAO y ejecutamos el guardado
            AdminCotizacionesDAO dao = new AdminCotizacionesDAO();
            boolean guardadoExitoso = dao.guardarCotizacion(idPedidoMedida, precio, fechaLimite, comentario);

            // Retornamos la respuesta al cliente
            if (guardadoExitoso) {
                out.print("{\"success\": true}");
            } else {
                out.print("{\"success\": false, \"mensaje\": \"Error interno al intentar registrar la cotización en la base de datos.\"}");
            }

        } catch (NumberFormatException e) {
            out.print("{\"success\": false, \"mensaje\": \"El precio o el identificador del pedido no tienen un formato numérico válido.\"}");
        } catch (Exception e) {
            out.print("{\"success\": false, \"mensaje\": \"Error crítico en el controlador: " + e.getMessage() + "\"}");
        }
        
        out.flush();
    }
}