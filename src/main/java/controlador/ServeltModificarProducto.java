package controlador;

import dao.PrendasDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Servelt encargado de comunicar o traer las modificaciones de algun producto

@WebServlet("/ModificarPrendaServlet")

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB por archivo
    maxRequestSize = 1024 * 1024 * 50     // 50MB total
)

public class ServeltModificarProducto extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. CAPTURA BÁSICA DE DATOS GLOBALES
            int idPrenda = Integer.parseInt(request.getParameter("idPrenda"));
            String nuevoNombre = request.getParameter("nombre");
            int idCategoria = Integer.parseInt(request.getParameter("categoria")); 
            String estado = request.getParameter("estado");
            String descripcion = request.getParameter("descripcion");
            String jsonImagenesViejas = request.getParameter("imagenesViejas"); 
            String jsonVariantes = request.getParameter("variantes"); // <-- NUEVO: Captura la colección de tallas

            PrendasDAO dao = new PrendasDAO();

            // 2. OBTENCIÓN DE DATOS ANTERIORES
            Map<String, Object> detallesAnteriores = dao.obtenerDetallesPrendaConVariantes(idPrenda);
            if (detallesAnteriores == null || detallesAnteriores.isEmpty()) {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se encontró la prenda original en el sistema.\"}");
                return;
            }
            
            String nombreOriginal = (String) detallesAnteriores.get("nombre");
            String tipo = (String) detallesAnteriores.get("tipo"); 

            // 3. DESERIALIZACIÓN NATIVA DEL JSON DE VARIANTES (Multitallaje)
            List<Map<String, Object>> listaVariantes = new ArrayList<>();
            
            if (jsonVariantes != null && !jsonVariantes.trim().isEmpty()) {
                String clean = jsonVariantes.trim();
                if (clean.startsWith("[")) clean = clean.substring(1);
                if (clean.endsWith("]")) clean = clean.substring(0, clean.length() - 1);
                
                // Separamos los objetos individuales del array: {"id":1,"talla":"M"...}
                String[] objetos = clean.split("\\},\\s*\\{");
                for (String obj : objetos) {
                    obj = obj.replace("{", "").replace("}", "");
                    Map<String, Object> varianteMapa = new HashMap<>();
                    String[] pares = obj.split(",");
                    
                    for (String par : pares) {
                        String[] kv = par.split(":");
                        if (kv.length == 2) {
                            String clave = kv[0].replace("\"", "").trim();
                            String valorRaw = kv[1].replace("\"", "").trim();
                            
                            if (clave.equals("id")) {
                                varianteMapa.put("id", Integer.parseInt(valorRaw));
                            } else if (clave.equals("talla")) {
                                varianteMapa.put("talla", valorRaw);
                            } else if (clave.equals("stock")) {
                                varianteMapa.put("stock", Integer.parseInt(valorRaw));
                            } else if (clave.equals("valor")) {
                                varianteMapa.put("valor", Double.parseDouble(valorRaw));
                            }
                        }
                    }
                    if (!varianteMapa.isEmpty()) {
                        listaVariantes.add(varianteMapa);
                    }
                }
            }

            // Validación de seguridad por si el Front envía un bloque vacío
            if (listaVariantes.isEmpty()) {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"El producto debe contener al menos una variante de talla válida.\"}");
                return;
            }

            // 4. PROCESAMIENTO DE IMÁGENES VIEJAS
            List<String> rutasFinales = new ArrayList<>();
            if (jsonImagenesViejas != null && !jsonImagenesViejas.trim().isEmpty()) {
                String limpias = jsonImagenesViejas.replace("[", "").replace("]", "").replace("\"", "");
                String[] fragmentos = limpias.split(",");
                for (String ruta : fragmentos) {
                    if (!ruta.trim().isEmpty()) {
                        rutasFinales.add(ruta.trim());
                    }
                }
            }

            // 5. PROCESAMIENTO DE IMÁGENES NUEVAS (BINARIOS)
            String rutaDestinoServer = request.getServletContext().getRealPath("/images/Prendas");
            File carpeta = new File(rutaDestinoServer);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            for (Part part : request.getParts()) {
                if (part.getName().startsWith("archivo_imagen_") && part.getSize() > 0) {
                    String nombreOriginalFile = part.getSubmittedFileName();
                    String nombreUnico = idPrenda + "_" + System.currentTimeMillis() + "_" + nombreOriginalFile;
                    
                    part.write(rutaDestinoServer + File.separator + nombreUnico);
                    rutasFinales.add("/images/Prendas/" + nombreUnico);
                }
            }

            // 6. EJECUCIÓN DE LA TRANSACCIÓN UNIFICADA EN EL DAO
            boolean exito = dao.actualizarProductoConVariantes(
                nombreOriginal, 
                nuevoNombre, 
                tipo, 
                idCategoria, 
                estado, 
                descripcion, 
                listaVariantes, 
                rutasFinales, 
                idPrenda
            );

            if (exito) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            } else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error al ejecutar la transacción de actualización en la Base de Datos.\"}");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Error de parseo numérico en el Servlet: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Formato numérico inválido en los datos enviados.\"}");
        } catch (Exception e) {
            System.out.println("Error crítico en ModificarPrendaServlet: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + e.getMessage() + "\"}");
        }
    }
}