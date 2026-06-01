package controlador; // Ajusta el paquete según tu proyecto

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

@WebServlet("/RegistrarPrendaServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB por archivo
    maxRequestSize = 1024 * 1024 * 50     // 50MB en total
)
public class ServeltAgregarprendas extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Captura de parámetros desde el formulario HTML
            String nombre = request.getParameter("nombreProducto");
            String talla = request.getParameter("talla");
            double precio = Double.parseDouble(request.getParameter("precio")); 
            int stock = Integer.parseInt(request.getParameter("stock")); 
            int idCategoria = Integer.parseInt(request.getParameter("categoria")); 
            String descripcion = request.getParameter("descripcion");
            
            // Lógica de negocio automática para el estado
            String estado = (stock > 0) ? "activa" : "inactiva";

            // 2. Preparar el directorio físico para las imágenes temporales
            List<String> rutasImagenes = new ArrayList<>();
            String rutaDestinoServer = request.getServletContext().getRealPath("/images/Prendas");
            File carpeta = new File(rutaDestinoServer);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // 3. Iterar los archivos subidos (Buscamos los prefijos del JavaScript de carga)
            for (Part part : request.getParts()) {
                if (part.getName().startsWith("archivo_imagen_") && part.getSize() > 0) {
                    String nombreOriginal = part.getSubmittedFileName();
                    
                    if (nombreOriginal != null && !nombreOriginal.trim().isEmpty()) {
                        // Usamos un identificador temporal basado en tiempo porque aún no tenemos el ID definitivo de MySQL
                        String nombreUnico = "NUEVO_" + System.currentTimeMillis() + "_" + nombreOriginal;
                        
                        // Guardar binario en el servidor
                        part.write(carpeta.getAbsolutePath() + File.separator + nombreUnico);
                        
                        // Guardar la ruta web relativa
                        rutasImagenes.add("/images/" + nombreUnico);
                    }
                }
            }

            // 4. Invocar al DAO de inserción relacional
            PrendasDAO dao = new PrendasDAO();
            boolean registradoExitoso = dao.registrarPrenda(nombre, precio, talla, idCategoria, stock, estado, descripcion, rutasImagenes);

            if (registradoExitoso) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            }
            
            else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se pudo insertar la prenda en la base de datos.\"}");
            }

        } 
        catch (NumberFormatException e) {
            System.out.println("Error numérico en ServletRegistrarProducto: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Verifica que el precio y el stock sean números válidos.\"}");
        } 
        catch (Exception e) {
            System.out.println("Error crítico en ServletRegistrarProducto: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + e.getMessage() + "\"}");
        }
    }
}
