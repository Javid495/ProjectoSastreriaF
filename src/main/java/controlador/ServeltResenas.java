package controlador;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig; // 🌟 IMPORTANTE
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import Dtos.IniciarSesion;
import Dtos.Resenas;
import modelo.ResenaDAO;

@WebServlet("/ResenasController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB máximo por archivo
    maxRequestSize = 1024 * 1024 * 50     // 50MB máximo por formulario completo
)
public class ServeltResenas extends HttpServlet {
    
    private final ResenaDAO resenaDAO = new ResenaDAO();
    private final Gson gson = new Gson();

    // GET: Recupera las reseñas de la prenda actual
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String idPrendaParam = request.getParameter("prendaId");
        if (idPrendaParam != null && !idPrendaParam.isEmpty()) {
            try {
                int prendaId = Integer.parseInt(idPrendaParam);
                List<Resenas> lista = resenaDAO.obtenerResenasPorPrenda(prendaId);
                out.print(this.gson.toJson(lista));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"ID de prenda no válido.\"}");
            }
        }
        out.flush();
    }

    // POST: Inserta un comentario nuevo si el usuario está validado
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        
        // 1. Comprobar sesión activa
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\",\"message\":\"Debes iniciar sesión para publicar una reseña.\"}");
            out.flush();
            return;
        }

        IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
        int idUsuario = usuarioLog.getId();
        
        // 🌟 Ahora gracias a @MultipartConfig, estos parámetros ya NO vendrán null
        String strPrendaId = request.getParameter("prendaId");
        String comentario = request.getParameter("comentario");

        // 🛡️ VALIDACIÓN EN SERVIDOR: El comentario es obligatorio por encima de la imagen
        if (comentario == null || comentario.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"El texto del comentario es obligatorio para guardar la reseña.\"}");
            out.flush();
            return;
        }

        int idPrenda = 0;
        
        try {
            idPrenda = Integer.parseInt(strPrendaId);
        } 
        
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"El ID de la prenda es inválido.\"}");
            out.flush();
            return;
        }
        
        String rutaImagenResena = "images/Resenas/default.png"; // Ruta por defecto
        
        try {
            Part filePart = request.getPart("imagen"); // Buscamos la propiedad 'imagen' del FormData
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + getSubmittedFileName(filePart);
                
                // Define la ruta en tu servidor físico donde se guardarán las fotos
                String uploadPath = getServletContext().getRealPath("") + File.separator + "images" + File.separator + "Resenas";
                File uploadDir = new File(uploadPath);
                
                if (!uploadDir.exists()) uploadDir.mkdirs();
                
                filePart.write(uploadPath + File.separator + fileName);
                rutaImagenResena = "images/Resenas/" + fileName; // Reemplazamos por la imagen real subida
            }
        } 
        
        catch (Exception e) {
            System.err.println("Aviso: No se subió imagen personalizada o falló su lectura, usando default. " + e.getMessage());
        }

        // Armamos el objeto DTO
        Resenas nuevaResena = new Resenas();
        nuevaResena.setUsuarioId(idUsuario);
        nuevaResena.setPrendaId(idPrenda);
        nuevaResena.setDescripcion(comentario.trim());
        nuevaResena.setImagenResena(rutaImagenResena);

        boolean exito = resenaDAO.agregarResena(nuevaResena);

        if (exito) {
            out.print("{\"status\":\"success\",\"message\":\"¡Reseña publicada con éxito!\"}");
        } 
        
        else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"No se pudo guardar la reseña en la base de datos.\"}");
        }
        out.flush();
    }

    // Helper para extraer el nombre original del archivo enviado en el header del Part
    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
            }
        }
        return "archivo_desconocido.png";
    }
}