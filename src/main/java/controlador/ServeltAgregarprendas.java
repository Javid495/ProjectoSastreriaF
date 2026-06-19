package controlador;

import modelo.PrendasDAO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Servelt de agregar prendas al catalogo

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
            // 1. Captura de parámetros base comunes de la prenda
            String nombre = request.getParameter("nombreProducto");
            String tipo = request.getParameter("tipoProducto"); // Asegúrate de enviarlo desde el JS
            int idCategoria = Integer.parseInt(request.getParameter("categoria")); 
            String descripcion = request.getParameter("descripcion");
            String estado = request.getParameter("estado"); // Recibido desde la validación JS

            // 2. EXTRAER LAS VARIANTES DINÁMICAS (Enviadas como string JSON dentro del FormData)
            String variantesJson = request.getParameter("variantes");
            if (variantesJson == null || variantesJson.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe incluir al menos una variante de talla y stock.");
            }
            
            Gson gson = new Gson();
            // Deserializamos el texto plano JSON a una Lista de Mapas amigable para el nuevo DAO
            Type listaTipo = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> variantes = gson.fromJson(variantesJson, listaTipo);

            // 3. Preparar el directorio físico para las imágenes
            List<String> rutasImagenes = new ArrayList<>();
            String rutaDestinoServer = request.getServletContext().getRealPath("/images/Prendas");
            File carpeta = new File(rutaDestinoServer);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // 4. Iterar y guardar los binarios de las imágenes
            for (Part part : request.getParts()) {
                if (part.getName().startsWith("archivo_imagen_") && part.getSize() > 0) {
                    String nombreOriginal = part.getSubmittedFileName();
                    
                    if (nombreOriginal != null && !nombreOriginal.trim().isEmpty()) {
                        String nombreUnico = "NUEVO_" + System.currentTimeMillis() + "_" + nombreOriginal;
                        part.write(carpeta.getAbsolutePath() + File.separator + nombreUnico);
                        rutasImagenes.add("/images/Prendas/" + nombreUnico);
                    }
                }
            }

            // 5. Invocación al nuevo método atómico del DAO
            PrendasDAO dao = new PrendasDAO();
            boolean registradoExitoso = dao.registrarProductoConVariantes(
                nombre, tipo, idCategoria, estado, descripcion, variantes, rutasImagenes
            );

            if (registradoExitoso) {
                response.getWriter().write("{\"status\": \"Exito\", \"mensaje\": \"Producto con variantes registrado correctamente.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se pudo insertar el lote de prendas en la transacción.\"}");
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.out.println("Error crítico en ServeltAgregarprendas: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error interno en el servidor.\"}");
        }
    }
}
