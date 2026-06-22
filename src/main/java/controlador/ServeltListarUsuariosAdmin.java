package controlador;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getsSets.IniciarSesion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.UsuariosDAO;

@WebServlet("/ListarUsuariosAdmin") // El endpoint real para el Administrador
public class ServeltListarUsuariosAdmin extends HttpServlet {

    private final UsuariosDAO usuarioDAO = new UsuariosDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. Obtenemos la lista de usuarios desde la base de datos
        List<IniciarSesion> listaUsuarios = usuarioDAO.listarTodosLosUsuarios();
        
        // 2. Mapeamos los datos para que coincidan con las llaves exactas del JS
        List<Map<String, Object>> listaFormateada = new ArrayList<>();
        
        for (IniciarSesion u : listaUsuarios) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("nombre", u.getUsuario()); // Tu propiedad 'usuario' se mapea como 'nombre' para el frontend
            map.put("email", u.getEmail());
            map.put("telefono", u.getTelefono());
            map.put("avatar", u.getImagen());
            
            listaFormateada.add(map);
        }

        // 3. Despachamos el JSON limpio al frontend
        out.print(this.gson.toJson(listaFormateada));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
