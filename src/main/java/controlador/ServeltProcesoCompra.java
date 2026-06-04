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
import java.io.PrintWriter;
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

            // 1. Extraemos los campos comunes a ambos flujos
            String direccion = extraerValorJson(jsonRaw, "direccion");
            String telefono = extraerValorJson(jsonRaw, "telefono");
            String metodoPago = extraerValorJson(jsonRaw, "metodoPago");
            String tipoPedido = extraerValorJson(jsonRaw, "tipoPedido");

            CompraPedidosDAO dao = new CompraPedidosDAO();
            boolean exito = false;

            // 🔀 BIFURCACIÓN DE FLUJOS: Evaluamos el tipo de pedido enviado por JS
            if ("A Medida".equalsIgnoreCase(tipoPedido)) {
                
                // =================================================================
                // 🧵 NUEVO FLUJO: PEDIDOS A MEDIDA (COTIZACIONES)
                // =================================================================
                int idCotizacion = extraerIntJson(jsonRaw, "CotizacionPedido_Id");
                
                // Llamamos a un nuevo método especializado en tu DAO para no tocar el anterior
                exito = dao.registrarFlujoCompletoCompraAMedida(idUsuario, direccion, telefono, metodoPago, tipoPedido, idCotizacion);
                
            } else {
                
                List<int[]> listaProductos = new ArrayList<>();
                
                // 1. Encontramos cada objeto individual {...} dentro del array JSON
                Pattern objetoPattern = Pattern.compile("\\{[^\\}]+\\}");
                Matcher objetoMatcher = objetoPattern.matcher(jsonRaw);

                while (objetoMatcher.find()) {
                    String bloqueObjeto = objetoMatcher.group();
                    
                    // Extraer ID (Soporta que desde el JS venga como "id" o como "idPrenda")
                    int idPrenda = 0;
                    Pattern pId = Pattern.compile("\"(?:id|idPrenda)\"\\s*:\\s*\"?(\\d+)\"?");
                    Matcher mId = pId.matcher(bloqueObjeto);
                    if (mId.find()) idPrenda = Integer.parseInt(mId.group(1));
                    
                    // Extraer Cantidad
                    int cantidad = 0;
                    Pattern pCant = Pattern.compile("\"cantidad\"\\s*:\\s*\"?(\\d+)\"?");
                    Matcher mCant = pCant.matcher(bloqueObjeto);
                    if (mCant.find()) cantidad = Integer.parseInt(mCant.group(1));
                    
                    // Extraer Precio o Total de Línea
                    double totalLinea = 0;
                    Pattern pPrecio = Pattern.compile("\"(?:precio|totalLinea)\"\\s*:\\s*\"?(\\d+(?:\\.\\d+)?)\"?");
                    Matcher mPrecio = pPrecio.matcher(bloqueObjeto);
                    if (mPrecio.find()) {
                        double valorNumerico = Double.parseDouble(mPrecio.group(1));
                        // Si el JSON traía "precio" unitario, lo multiplicamos por la cantidad
                        if (bloqueObjeto.contains("\"precio\"") && !bloqueObjeto.contains("\"totalLinea\"")) {
                            totalLinea = valorNumerico * cantidad;
                        } else {
                            totalLinea = valorNumerico;
                        }
                    }
                    
                    // Solo si encontramos datos válidos, lo agregamos a la lista
                    if (idPrenda > 0 && cantidad > 0) {
                        listaProductos.add(new int[]{idPrenda, cantidad, (int) totalLinea});
                    }
                }

                // Alerta preventiva por si el array sigue vacío por culpa del envío en el JS
                if (listaProductos.isEmpty()) {
                    System.out.println("⚠️ ALERTA: No se pudo extraer ningún producto del JSON recibido: " + jsonRaw);
                    response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"El carrito enviado no tiene un formato válido.\"}");
                    return;
                }

                exito = dao.registrarFlujoCompletoCompra(idUsuario, direccion, telefono, metodoPago, tipoPedido, listaProductos);
            }

            // Respuesta unificada para el JavaScript
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

    // Tu método original para extraer Strings entre comillas ("llave":"valor")
    private String extraerValorJson(String json, String llave) {
        Pattern p = Pattern.compile("\"" + llave + "\":\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    // 🎯 NUEVO HELPER: Extrae números enteros sin comillas del JSON ("idCotizacion":4)
    private int extraerIntJson(String json, String llave) {
    // El \"? le dice al motor de regex: "puede o no haber una comilla aquí"
        Pattern p = Pattern.compile("\"" + llave + "\"\\s*:\\s*\"?(\\d+)\"?");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }
}
