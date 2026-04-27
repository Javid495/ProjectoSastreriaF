
//Archivo para probar la conexion web del projecto

package controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import modelo.ClaseConexion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/test-db")

public class ServerPrueba extends HttpServlet {
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json"); // Le decimos a JS que responderemos datos
        PrintWriter out = response.getWriter();
        
        Connection cn = ClaseConexion.getConexion();
        
        if (cn != null) {
            out.print("{\"status\": \"success\", \"message\": \"¡Conexión exitosa desde la Web!\"}");
        } else {
            out.print("{\"status\": \"error\", \"message\": \"No se pudo conectar a MySQL\"}");
        }
    }
}
