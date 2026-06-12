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
        
        try {
            IniciarSesion cuentaUsuario = (IniciarSesion) session.getAttribute("PerfilUsuario");
            int idUsuario = cuentaUsuario.getId(); 

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                buffer.append(linea);
            }
            String jsonRaw = buffer.toString();

            // 🎯 EXTRAER ACCIÓN: Saber si es inserción temporal (B1) o pedido definitivo (B2)
            String accion = extraerValorJson(jsonRaw, "accion");

            CompraPedidosDAO dao = new CompraPedidosDAO();
            boolean exito = false;

            // =================================================================
            // 🛒 CASO 1: REGISTRO TEMPORAL (Al hacer clic en "Realizar Compra")
            // =================================================================
            if ("temporal".equalsIgnoreCase(accion)) {
                
                List<int[]> listaProductos = parsearProductosDesdeJson(jsonRaw);

                if (listaProductos.isEmpty()) {
                    System.out.println("⚠️ ALERTA: No se encontraron productos para el carrito temporal.");
                    response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"El carrito no contiene productos válidos.\"}");
                    return;
                }

                // Invoca al método temporal que limpia registros previos e inserta en Carrito y DetallesCarrito
                exito = dao.registrarCarritoTemporal(idUsuario, listaProductos);

            // =================================================================
            // 🚀 CASO 2: PROCESAR COMPRA DEFINITIVA (Al enviar el Formulario)
            // =================================================================
            } else {
                
                // Extraemos los campos comunes del formulario final
                String direccion = extraerValorJson(jsonRaw, "direccion");
                String telefono = extraerValorJson(jsonRaw, "telefono");
                String metodoPago = extraerValorJson(jsonRaw, "metodoPago");
                String tipoPedido = extraerValorJson(jsonRaw, "tipoPedido");

                // 🔀 BIFURCACIÓN DE COMPRA DEFINITIVA
                if ("A Medida".equalsIgnoreCase(tipoPedido)) {
                    
                    // 🧵 Flujo definitivo para pedidos personalizados
                    int idCotizacion = extraerIntJson(jsonRaw, "CotizacionPedido_Id");
                    exito = dao.confirmarPedidoAMedidaDefinitivo(idUsuario, direccion, telefono, metodoPago, tipoPedido, idCotizacion);
                    
                } else {
                    
                    // 🛍️ Flujo definitivo para catálogo regular
                    List<int[]> listaProductos = parsearProductosDesdeJson(jsonRaw);

                    if (listaProductos.isEmpty()) {
                        response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"El pedido no tiene productos válidos.\"}");
                        return;
                    }

                    // Guarda Pedido, DetallesPedido, descuenta stock y destruye el carrito temporal
                    exito = dao.confirmarPedidoDefinitivo(idUsuario, direccion, telefono, metodoPago, tipoPedido, listaProductos);
                }
            }

            // Respuesta unificada para tu fetch de JavaScript
            if (exito) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            } else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error transaccional en la base de datos.\"}");
            }

        } catch (Throwable t) {
            System.out.println("====== ALERTA DE ERROR EN SERVLET ======");
            t.printStackTrace(); 
            System.out.println("========================================");
    
            String mensajeSeguro = "Error interno en el servidor.";
            if (t.getMessage() != null) {
                mensajeSeguro = t.getMessage()
                                 .replace("\\", "\\\\")  
                                 .replace("\"", "'")     
                                 .replace("\n", " ")     
                                 .replace("\r", " ")     
                                 .replace("\t", " ")     
                                 .trim();
            }
    
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + mensajeSeguro + "\"}");
        }
    }

    // 🛠️ HELPER MODULAR: Aísla la lógica de conversión de productos para no duplicar código
    private List<int[]> parsearProductosDesdeJson(String jsonRaw) {
        List<int[]> listaProductos = new ArrayList<>();
        
        Pattern objetoPattern = Pattern.compile("\\{[^\\}]+\\}");
        Matcher objetoMatcher = objetoPattern.matcher(jsonRaw);

        while (objetoMatcher.find()) {
            String bloqueObjeto = objetoMatcher.group();
            
            int idPrenda = 0;
            Pattern pId = Pattern.compile("\"(?:id|idPrenda)\"\\s*:\\s*\"?(\\d+)\"?");
            Matcher mId = pId.matcher(bloqueObjeto);
            if (mId.find()) idPrenda = Integer.parseInt(mId.group(1));
            
            int cantidad = 0;
            Pattern pCant = Pattern.compile("\"cantidad\"\\s*:\\s*\"?(\\d+)\"?");
            Matcher mCant = pCant.matcher(bloqueObjeto);
            if (mCant.find()) cantidad = Integer.parseInt(mCant.group(1));
            
            double totalLinea;
            Pattern pPrecio = Pattern.compile("\"precio\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?");
            Pattern pTotalLinea = Pattern.compile("\"totalLinea\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?");

            Matcher mPrecio = pPrecio.matcher(bloqueObjeto);
            Matcher mTotalLinea = pTotalLinea.matcher(bloqueObjeto);

            double precioUnitario = -1;
            double totalLineaJson = -1;

            if (mPrecio.find()) precioUnitario = Double.parseDouble(mPrecio.group(1));
            if (mTotalLinea.find()) totalLineaJson = Double.parseDouble(mTotalLinea.group(1));

            if (totalLineaJson != -1) {
                totalLinea = totalLineaJson;
            } else if (precioUnitario != -1) {
                totalLinea = precioUnitario * cantidad;
            } else {
                totalLinea = 0; 
            }
            
            if (idPrenda > 0 && cantidad > 0) {
                listaProductos.add(new int[]{idPrenda, cantidad, (int) totalLinea});
            }
        }
        return listaProductos;
    }

    // Métodos utilitarios de análisis de cadenas JSON
    private String extraerValorJson(String json, String llave) {
        Pattern p = Pattern.compile("\"" + llave + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private int extraerIntJson(String json, String llave) {
        Pattern p = Pattern.compile("\"" + llave + "\"\\s*:\\s*\"?(\\d+)\"?");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}
