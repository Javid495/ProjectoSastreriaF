package controlador;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Dtos.IniciarSesion;
import Dtos.HistorialUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.UsuariosDAO;
import modelo.HistorialUsuarioDAO;

@WebServlet("/DetallesUsuarioAdmin") // Esta será la URL real a la que le apuntará el JS
public class ServeltHistorialUsuario extends HttpServlet {

    private final UsuariosDAO usuarioDAO = new UsuariosDAO();
    private final HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. Capturamos el ID que el Admin mandó desde la URL del navegador
        String idParam = request.getParameter("idUsuario");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                int idUsuario = Integer.parseInt(idParam);

                // 2. Consultamos los datos usando tus DAOs existentes
                IniciarSesion datosUsuario = usuarioDAO.obtenerDatosUsuario(idUsuario);
                List<HistorialUsuario> logAcciones = historialDAO.obtenerHistorialPorUsuario(idUsuario);

                // 3. Empaquetamos todo en un mapa para estructurar el JSON
                Map<String, Object> respuestaJson = new HashMap<>();
                respuestaJson.put("usuario", datosUsuario);
                respuestaJson.put("historial", logAcciones);

                // 4. Enviamos el paquete completo al frontend
                out.print(this.gson.toJson(respuestaJson));

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"El ID de usuario no es válido.\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"No se proporcionó un ID de usuario.\"}");
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
