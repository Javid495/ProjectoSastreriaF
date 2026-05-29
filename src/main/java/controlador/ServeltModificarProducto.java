package controlador; // Ajusta el paquete según la estructura de tu proyecto

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
import java.util.List;

@WebServlet("/ModificarPrendaServlet")
// Asegura la capacidad de recibir archivos grandes del explorador de la sastreria
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB por archivo
    maxRequestSize = 1024 * 1024 * 50     // 50MB en total de la petición
)

public class ServeltModificarProducto extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Captura de parámetros desde el FormData del JS
            int idPrenda = Integer.parseInt(request.getParameter("idPrenda"));
            String nombre = request.getParameter("nombre");
            double precio = Double.parseDouble(request.getParameter("precio")); 
            String talla = request.getParameter("talla");
            int idCategoria = Integer.parseInt(request.getParameter("categoria")); 
            int stock = Integer.parseInt(request.getParameter("stock")); 
            String estado = request.getParameter("estado");
            String descripcion = request.getParameter("descripcion");
            
            // Recibe la cadena stringified: ["images/Prendas/foto1.png", ...]
            String jsonImagenesViejas = request.getParameter("imagenesViejas"); 

            PrendasDAO dao = new PrendasDAO();
            
            // 2. Ejecutar la actualización de los datos principales en la tabla 'Prendas'
            boolean prendaActualizada = dao.actualizarPrenda(idPrenda, nombre, precio, talla, idCategoria, stock, estado, descripcion);
            
            if (prendaActualizada) {
                // Lista dinámica para unificar las rutas finales que se van a indexar en la tabla 'imagenes'
                List<String> rutasFinales = new ArrayList<>();
                
                // Procesar las rutas viejas que el administrador no borró de la vista
                if (jsonImagenesViejas != null && !jsonImagenesViejas.trim().isEmpty()) {
                    // Limpieza simple del formato JSON array plano [ "ruta1", "ruta2" ]
                    String limpias = jsonImagenesViejas.replace("[", "").replace("]", "").replace("\"", "");
                    String[] fragmentos = limpias.split(",");
                    for (String ruta : fragmentos) {
                        if (!ruta.trim().isEmpty()) {
                            rutasFinales.add(ruta.trim());
                        }
                    }
                }
                
                // Definición de la carpeta física destino dentro de la metadata desplegada en tu servidor
                String rutaDestinoServer = request.getServletContext().getRealPath("/images/Prendas");
                File carpeta = new File(rutaDestinoServer);
                if (!carpeta.exists()) {
                    carpeta.mkdirs(); // Inicializa el directorio si no existía previamente
                }
                
                // 3. Procesar iterativamente los binarios adjuntos por el input file oculto
                for (Part part : request.getParts()) {
                    // Detecta si la llave coincide con el prefijo dinámico "archivo_imagen_" y contiene data real
                    if (part.getName().startsWith("archivo_imagen_") && part.getSize() > 0) {
                        String nombreOriginal = part.getSubmittedFileName();
                        
                        // Generación de un nombre único e irrepetible para evitar sobreescribir archivos existentes
                        String nombreUnico = idPrenda + "_" + System.currentTimeMillis() + "_" + nombreOriginal;
                        
                        // Guarda físicamente el archivo binario en el disco duro del servidor
                        part.write(rutaDestinoServer + File.separator + nombreUnico);
                        
                        // Guarda la ruta relativa web limpia que solicita la base de datos
                        rutasFinales.add("images/Prendas/" + nombreUnico);
                    }
                }
                
                // 4. CORRECCIÓN: Llamamos al nombre real del método corregido en tu DAO
                dao.sincronizarImagenesPrenda(idPrenda, rutasFinales);
                
                // Envía confirmación de éxito al frontend
                response.getWriter().write("{\"status\": \"Exito\"}");
            } else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se pudo actualizar la tabla de Prendas en la base de datos.\"}");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Error de parseo numérico en el Servlet: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Formato numérico inválido en precio, stock o id.\"}");
        } catch (Exception e) {
            System.out.println("Error crítico en ModificarPrendaServlet: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + e.getMessage() + "\"}");
        }
    }
}