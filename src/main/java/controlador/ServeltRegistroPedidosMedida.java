package controlador;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import jakarta.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import dao.PedidosMedidaDao;
import modelo.IniciarSesion;
import modelo.DetallesPedidoMedida;

//Servelt quien registra nuevos pedidos a medida

//Creamos uestra relacion con el frontend
@WebServlet("/RegistrarPedidoMedida")

//verificar
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class ServeltRegistroPedidosMedida extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {

        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        PrintWriter out = respuesta.getWriter();
        
        HttpSession session = solicitud.getSession(false);
        
        // Verificamos sesión activa
        if (session == null || session.getAttribute("PerfilUsuario") == null) {
            out.print("{\"success\": false, \"mensaje\": \"Sesión vencida o inválida.\"}");
            return;
        }

        try {
            IniciarSesion user = (IniciarSesion) session.getAttribute("PerfilUsuario");
            
            // Recibimos parámetros normales del formulario
            String tipoPrenda = solicitud.getParameter("tipoPrenda");
            String telas = solicitud.getParameter("telas");
            String talla = solicitud.getParameter("talla");
            String descripcion = solicitud.getParameter("descripcion");
            String medidas = solicitud.getParameter("medidas");
            
            // Añadimos la talla a la descripción o al formato de medidas para no perder el dato
            String desgloseDescripcion = "Talla solicitada: " + talla + " | " + descripcion;

            // Manejo de la subida de la imagen
            Part part = solicitud.getPart("fotoReferencia");
            String rutaRelativaImagen = null;
            
            if (part != null && part.getSize() > 0) {
                // Directorio interno de guardado
                String rutaDestino = getServletContext().getRealPath("/") + "images" + File.separator + "PedidosMedida";
                File carpeta = new File(rutaDestino);
                if (!carpeta.exists()) carpeta.mkdirs();
                
                // Nombre único para que no se pisen archivos de distintos usuarios
                String nombreArchivo = System.currentTimeMillis() + "_" + part.getSubmittedFileName();
                part.write(rutaDestino + File.separator + nombreArchivo);
                
                // Guardamos la ruta que usará el frontend para leerla
                rutaRelativaImagen = "images/PedidosMedida/" + nombreArchivo;
            }

            // Poblamos el modelo
            DetallesPedidoMedida nuevoPedido = new DetallesPedidoMedida();
            nuevoPedido.setIdUsuario(user.getId());
            nuevoPedido.setMedidas(medidas);
            nuevoPedido.setTipoPrenda(tipoPrenda);
            nuevoPedido.setTela(telas);
            nuevoPedido.setDescripcion(desgloseDescripcion);
            nuevoPedido.setImagenReferencia(rutaRelativaImagen);

            // Enviamos al DAO
            PedidosMedidaDao dao = new PedidosMedidaDao();
            boolean insertado = dao.registrarSolicitudMedida(nuevoPedido);

            if (insertado) {
                out.print("{\"success\": true}");
            } else {
                out.print("{\"success\": false, \"mensaje\": \"Error al guardar en el sistema de datos.\"}");
            }

        } catch (Exception e) {
            out.print("{\"success\": false, \"mensaje\": \"Error en el procesamiento del formulario: " + e.getMessage() + "\"}");
        }
        out.flush();
    }
}