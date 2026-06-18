package controlador;

import dao.EliminarPrendasDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Servlet encargado de cambiar el estado a 'eliminada' a múltiples prendas en lote
@WebServlet("/EliminarVariasPrendas")
public class ServeltEliminarVarios extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. LEER EL CUERPO JSON ENVIADO POR EL FETCH
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String linea;
            while ((linea = reader.readLine()) != null) {
                buffer.append(linea);
            }
            String jsonRaw = buffer.toString(); // Ejemplo: {"ids":[1,2,3]}

            // 2. EXTRAER LOS NÚMEROS DE FORMA NATIVA (Con Expresiones Regulares)
            List<Integer> listaIds = new ArrayList<>();
            Pattern p = Pattern.compile("\\d+"); // Busca secuencias de dígitos
            Matcher m = p.matcher(jsonRaw);
            
            while (m.find()) {
                listaIds.add(Integer.parseInt(m.group()));
            }

            // Validación de seguridad por si el array venía vacío
            if (listaIds.isEmpty()) {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"No se recibieron IDs válidos para procesar.\"}");
                return;
            }

            System.out.println("Servlet: Procesando la deshabilitación masiva de los IDs: " + listaIds);

            // 3. INVOCAR AL DAO REUTILIZANDO EL MÉTODO DE ACTUALIZACIÓN EN MASA
            EliminarPrendasDAO dao = new EliminarPrendasDAO();
            boolean exito = dao.eliminarPrendasEnMasa(listaIds);

            // 4. RESPUESTA AL FRONTEND
            if (exito) {
                response.getWriter().write("{\"status\": \"Exito\"}");
            } else {
                response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Hubo un problema al actualizar el estado del lote en la base de datos.\"}");
            }

        } catch (Exception e) {
            System.out.println("Error crítico en ServeltEliminarVarios: " + e.getMessage());
            response.getWriter().write("{\"status\": \"Error\", \"mensaje\": \"Error interno en el servidor: " + e.getMessage() + "\"}");
        }
    }
}