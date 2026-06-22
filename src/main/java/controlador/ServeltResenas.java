package controlador;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import getsSets.IniciarSesion;
import getsSets.Resenas;
import modelo.ResenaDAO;

@WebServlet("/ResenasController")
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
            int prendaId = Integer.parseInt(idPrendaParam);
            List<Resenas> lista = resenaDAO.obtenerResenasPorPrenda(prendaId);
            out.print(this.gson.toJson(lista));
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
        
        // REQUISITO CLAVE: Comprobar sesión activa antes de dejar comentar
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\",\"message\":\"Debes iniciar sesión para publicar una reseña.\"}");
            out.flush();
            return;
        }

        // Recuperamos el DTO de sesión del cliente
        IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
        
        int idUsuario = usuarioLog.getId();
        int idPrenda = Integer.parseInt(request.getParameter("prendaId"));
        String comentario = request.getParameter("comentario");
        
        // Manejo provisional de imagen de reseña por defecto si no se carga una personalizada
        String rutaImagenResena = "images/Resenas/default.png"; 

        Resenas nuevaResena = new Resenas();
        nuevaResena.setUsuarioId(idUsuario);
        nuevaResena.setPrendaId(idPrenda);
        nuevaResena.setDescripcion(comentario);
        nuevaResena.setImagenResena(rutaImagenResena);

        boolean exito = resenaDAO.agregarResena(nuevaResena);

        if (exito) {
            out.print("{\"status\":\"success\",\"message\":\"¡Reseña publicada con éxito!\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"No se pudo guardar la reseña en la base de datos.\"}");
        }
        out.flush();
    }
}
