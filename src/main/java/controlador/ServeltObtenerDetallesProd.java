package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.gson.Gson;

import Dtos.IniciarSesion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.PopularesDAO;
import modelo.PrendasDAO;
import modelo.UsuariosDAO;

//Servelt encargado de manejar los detalles de los productos

//El webServelt me hace o me indica una ruta por la cual el frontend se puede comunicar con este archivo
@WebServlet("/ObtenerProductosDetalle")

public class ServeltObtenerDetallesProd extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String idParam = request.getParameter("id");
        
        try (PrintWriter out = response.getWriter()) {
        
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el id del producto");
                return;
            }
            
            int id = Integer.parseInt(idParam);
            
            PrendasDAO dao = new PrendasDAO();
            Map<String, Object> prendaDetalle = dao.obtenerDetallesPrendaConVariantes(id);
            
            if (prendaDetalle != null) {
                
                // 1. Registro global de la prenda (Suma de populares que ya tenías)
                PopularesDAO popularesDAO = new PopularesDAO();
                popularesDAO.registrarVisita(id);
                
                // 2. 🆕 REGISTRO PERSONALIZADO: Historial de prendas recientes
                // Intentamos capturar la sesión del usuario si existe
                HttpSession session = request.getSession(false);
                
                if (session != null && session.getAttribute("PerfilUsuario") != null) {
                    // Recuperamos el DTO de la sesión de forma limpia
                    IniciarSesion usuarioLog = (IniciarSesion) session.getAttribute("PerfilUsuario");
                    int usuarioId = usuarioLog.getId();
                    
                    // Instanciamos el DAO de usuarios para guardar el registro
                    UsuariosDAO usuarioDAO = new UsuariosDAO();
                    usuarioDAO.registrarPrendaReciente(usuarioId, id);
                } 
                
                else {
                    System.out.println("ℹ️ [ServletDetalle] Un usuario invitado vio la prenda " + id + ". No se guarda historial.");
                }
                
                // Despachamos la respuesta JSON original para el frontend
                Gson gson = new Gson();
                // una vez estabecidos lo pasamos a un formato .json qu epuede interpretar js
                String json = gson.toJson(prendaDetalle);
                out.print(json);
                
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado o sin existencias");
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID no válido");
        }
    }
}

