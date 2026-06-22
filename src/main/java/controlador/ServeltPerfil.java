package controlador;

import com.google.gson.Gson;
import modelo.UsuariosDAO;
import getsSets.IniciarSesion;
import getsSets.PrendasRecientes;
import getsSets.Pedidos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/PerfilUsuario")
public class ServeltPerfil extends HttpServlet {
    private UsuariosDAO usuarioDAO = new UsuariosDAO();
    private Gson gson = new Gson();

    // Enviar toda la información estructurada al cargar la página de perfil
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        
        // 1. Validamos usando la clave correcta de tu Login: "PerfilUsuario"
        if (session != null && session.getAttribute("PerfilUsuario") != null) {
            
            // 2. Recuperamos el objeto DTO completo que está almacenado en la sesión
            IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
            
            // 3. Extraemos el ID numérico real desde el objeto
            int usuarioId = usuarioLog.getId();
            
            // Consultamos los tres conjuntos de datos usando las clases de getsSets
            IniciarSesion datosUsuario = usuarioDAO.obtenerDatosUsuario(usuarioId);
            List<PrendasRecientes> recientes = usuarioDAO.obtenerPrendasRecientes(usuarioId);
            List<Pedidos> historial = usuarioDAO.obtenerHistorialPedidos(usuarioId);
            
            // Empaquetamos todo en una estructura de Mapa dinámico
            Map<String, Object> respuestaJson = new HashMap<>();
            respuestaJson.put("usuarioId", datosUsuario.getId());
            respuestaJson.put("nombre", datosUsuario.getUsuario());
            respuestaJson.put("correo", datosUsuario.getEmail());
            respuestaJson.put("telefono", datosUsuario.getTelefono());
            respuestaJson.put("imagenAvatar", datosUsuario.getImagen());
            respuestaJson.put("productosRecientes", recientes);
            respuestaJson.put("historialPedidos", historial);
            
            // Despachamos el JSON limpio
            out.print(this.gson.toJson(respuestaJson));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Acceso denegado. Sin sesión activa.\"}");
        }
        out.flush();
    }

    // Recibir la actualización limpia del formulario
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"status\":\"error\",\"message\":\"Sesión expirada o inválida\"}");
            out.flush();
            return;
        }

        IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
        int usuarioId = usuarioLog.getId();

        String nombre = request.getParameter("nombre");
        String telefono = request.getParameter("telefono");
        String correo = request.getParameter("correo");
        String imagenAvatar = request.getParameter("imagenAvatar"); // 🌟 Atrapamos la ruta enviada por JS

        // Enviamos el parámetro extra al DAO
        boolean modificado = usuarioDAO.actualizarPerfil(usuarioId, nombre, telefono, correo, imagenAvatar);

        if (modificado) {
            
            // Actualizamos de forma reactiva los datos en el objeto de sesión
            usuarioLog.setUsuario(nombre);
            usuarioLog.setEmail(correo);
            usuarioLog.setTelefono(telefono);
            usuarioLog.setImagen(imagenAvatar); // 🌟 Sincronizamos el nuevo avatar en sesión

            out.print("{\"status\":\"success\",\"message\":\"Información guardada con éxito\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"No se pudieron guardar las modificaciones en la base de datos\"}");
        }
        out.flush();
    }
}
