package controlador;

import dao.CompraPedidosDAO;
import modelo.IniciarSesion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/ProcesarCompraServlet")
public class ServeltProcesoCompra extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Sesión inválida.\"}");
            return;
        }
        
        // Convertimos el catch a Throwable para capturar TODO (Errores y Excepciones)
        try {
            //Recuperamos el objeto completo y usamos su método GETTER
            IniciarSesion cuentaUsuario = (IniciarSesion) session.getAttribute("PerfilUsuario");
            

            // Hacemos referencia al getter del modelo iniciarsesion
            int idUsuario = cuentaUsuario.getId(); 

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                buffer.append(linea);
            }
            String jsonRaw = buffer.toString();

            String direccion = extraerValorJson(jsonRaw, "direccion");
            String telefono = extraerValorJson(jsonRaw, "telefono");
            String metodoPago = extraerValorJson(jsonRaw, "metodoPago");
            String tipoPedido = extraerValorJson(jsonRaw, "tipoPedido");

            List<int[]> listaProductos = new ArrayList<>();
            Pattern p = Pattern.compile("\"idPrenda\"\\s*:\\s*(\\d+)\\s*,\\s*\"cantidad\"\\s*:\\s*(\\d+)\\s*,\\s*\"totalLinea\"\\s*:\\s*(\\d+(?:\\.\\d+)?)");
            Matcher m = p.matcher(jsonRaw);

            while (m.find()) {
                int idPrenda = Integer.parseInt(m.group(1));
                int cantidad = Integer.parseInt(m.group(2));
                double totalLinea = Double.parseDouble(m.group(3));
                listaProductos.add(new int[]{idPrenda, cantidad, (int) totalLinea});
            }

            CompraPedidosDAO dao = new CompraPedidosDAO();
            boolean exito = dao.registrarFlujoCompletoCompra(idUsuario, direccion, telefono, metodoPago, tipoPedido, listaProductos);

            if (exito) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            } else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error transaccional en la base de datos.\"}");
            }

        } catch (Throwable t) {
            //En caso de un error identifica el error
            System.out.println("====== ALERTA DE ERROR EN SERVLET ======");
            t.printStackTrace(); 
            System.out.println("========================================");
    
            // 2. Limpieza absoluta del mensaje para que JavaScript NUNCA se rompa
            String mensajeSeguro = "Error interno en el servidor.";
            if (t.getMessage() != null) {
            mensajeSeguro = t.getMessage()
                         .replace("\\", "\\\\")  // Escapa barras invertidas
                         .replace("\"", "'")     // Cambia comillas dobles por simples
                         .replace("\n", " ")     // Elimina saltos de línea
                         .replace("\r", " ")     // Elimina retornos de carro
                         .replace("\t", " ")     // Elimina tabulaciones
                         .trim();
            }
    
        // 3. Enviamos el JSON limpio
        response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + mensajeSeguro + "\"}");
        }
    }

    private String extraerValorJson(String json, String llave) {
        Pattern p = Pattern.compile("\"" + llave + "\":\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }
}
