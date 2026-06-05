package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import dao.AdminCotizacionesDAO;
import modelo.Prendas; // ¡No olvides importar tu modelo!

@WebServlet("/AdminCotizaciones")//Se define la url publica para que javascrip pueda ubicarlo a travez del fetch
public class ServeltAdminCotizaciones extends HttpServlet {

    //Usamos el meto doGet para atrapar las peticiones que se hacen por medio del enlace:
    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        //Dejamos clara la respuesta al 
        //Usuario o al cliete
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        
        //Es quien me construye la respues para el backend
        PrintWriter out = respuesta.getWriter();
        
        //Captura la variable que estamos mandando por el enlace
        //En este caso la accion
        String accion = solicitud.getParameter("accion");
        
        //Intanciamos la variable que se comunicara con el dao o quien se comunica con la base de datos
        AdminCotizacionesDAO dao = new AdminCotizacionesDAO();

        //Si el frotend o el clietne pregunta cuantos pedidos nuevos hay 
        //Sea para cotizar o que sean nuevos
        if ("contar".equals(accion)) {
            //Llamos a los dao que cumplen ccon la peticion
            int cotizar = dao.contarPendientesPorCotizar();
            int nuevos = dao.contarNuevosPedidos();
            
            // Retornamos un único objeto JSON con ambas propiedades
            out.print("{\"pedidosCotizar\":" + cotizar + ", \"nuevosPedidos\":" + nuevos + "}");
          
          //En caso de que sea listar
        } else if ("listar".equals(accion)) {
            
            //Se solicita al dato la lista de pedidos que estna pendientes
            List<String[]> pendientes = dao.listarPedidosPorCotizar();
            
            //Iniciamos un construtor para poder retornar una respuesta al cliente
            StringBuilder json = new StringBuilder("[");
            
            //recorre cada registro que devuelve la base de datos
            for (int i = 0; i < pendientes.size(); i++) {
                String[] item = pendientes.get(i);
                json.append("{");
                json.append("\"id\":").append(item[0]).append(",");
                json.append("\"email\":\"").append(item[1]).append("\",");
                json.append("\"tipo\":\"").append(item[2]).append("\",");
                json.append("\"tela\":\"").append(item[3]).append("\",");
                json.append("\"medidas\":\"").append(item[4]).append("\",");
                json.append("\"descripcion\":\"").append(item[5]).append("\",");
                json.append("\"imagen\":\"").append(item[6]).append("\"");
                json.append("}");
                if (i < pendientes.size() - 1) json.append(",");
            }
            //Cierra o envuelve cada registro
            json.append("]");
            
            //Envia la lista construida la frontend
            out.print(json.toString());
            
        } 
        
        //En caso de que la respuesta sea bajo en stock 
        else if ("bajoStock".equals(accion)) {
            
            //Revisa en la tabla prenda toda insetcion que sea menor a 5
            List<Prendas> bajoStock = dao.listarPrendasBajasStock(5);
            
            
            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < bajoStock.size(); i++) {
                
                //Usamos el modelo de prendas para revisar quienes estna bajo de stock
                Prendas item = bajoStock.get(i);
                json.append("{");
                json.append("\"id\":").append(item.getId()).append(",");
                json.append("\"nombre\":\"").append(item.getNombre()).append("\",");
                json.append("\"precio\":").append(item.getValor()).append(","); // Mapeado de getValor()
                json.append("\"stock\":").append(item.getStock()).append(",");   // Mapeado de getStock()
                json.append("\"imagen\":\"").append(item.getImagen()).append("\""); // Mapeado de getImagen()
                json.append("}");
                if (i < bajoStock.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
        out.flush();
    }
}
