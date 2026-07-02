package controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import modelo.AdminCotizacionesDAO;
import Dtos.Prendas; 


//Valida los pedidos a medida nuevo que esperan cotizacion

@WebServlet("/AdminCotizaciones") //Es el indicador o la bandera en el cual puedo acceder a travez de fetch

//Al momento de extender la clase como HttpServelt este se vuleve capaz de escuchar y responder a las peticiones que se hacen
public class ServeltAdminCotizaciones extends HttpServlet {

    @Override
    //Se realiza un metodo do get para hacer peticiones de lectura  de datos 
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        
        //Se le informa al navegador el tipo de dato que se le esta retornando
        //al frontend
        respuesta.setContentType("application/json");
        respuesta.setCharacterEncoding("UTF-8");
        
        //El printWrite es el encargado de escribit la respuesta que se retorna al usuario(Frontend)
        PrintWriter out = respuesta.getWriter();
        
        //Del hash obtenemos el parametor espefico de accion
        String accion = solicitud.getParameter("accion");
        
        //Declaramos o referenciamos la clase AdminCotizacionesDAO
        AdminCotizacionesDAO dao = new AdminCotizacionesDAO();

        // Metod que hace contento de los nuevas solicitudes
        if ("contar".equals(accion)) {
            
            //ejecutamos y traemos el resultado del metodo contarPendientespor cotizar
            int cotizar = dao.contarPendientesPorCotizar();
            
            //Y lo mismo realizamos para los nuevos pedidos
            int nuevos = dao.contarNuevosPedidos();
            
            // Retorna "cantidad" Tanto de nuevos pedidos como a cotizar.
            out.print("{\"cantidad\":" + cotizar + ", \"pedidosCotizar\":" + cotizar + ", \"nuevosPedidos\":" + nuevos + "}");
          
        // Listar las cotizaciones de pedidos a medida
        } 
        
        else if ("listar".equals(accion)) {
            List<String[]> pendientes = dao.listarPedidosPorCotizar();
            
            //String builder es la forma en la cual empizo o construyo mi respuesta al fontend
            StringBuilder json = new StringBuilder("[");
            
            //Medidante un ciclo for voy recorriendo y enlistando cada resultado que me retorna el dao
            for (int i = 0; i < pendientes.size(); i++) {
                
                String[] item = pendientes.get(i);
                json.append("{");
                json.append("\"id\":").append(item[0]).append(",");
                json.append("\"email\":\"").append(escaparJSON(item[1])).append("\",");
                json.append("\"tipo\":\"").append(escaparJSON(item[2])).append("\",");
                json.append("\"tela\":\"").append(escaparJSON(item[3])).append("\",");
                json.append("\"medidas\":\"").append(escaparJSON(item[4])).append("\",");
                json.append("\"descripcion\":\"").append(escaparJSON(item[5])).append("\",");
                json.append("\"imagen\":\"").append(escaparJSON(item[6])).append("\"");
                json.append("}");
                
                if (i < pendientes.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
            
        // Verifica los productos que se encuentran bajos de stock
        } 
        
        //Si llega ser bajo de stock
        else if ("bajoStock".equals(accion)) {
            // Revisa en la tabla prendas todo lo que sea menor o igual a 5 unidades
            List<Prendas> bajoStock = dao.listarPrendasBajasStock(5);
            
            //Creamos nuestra estrutura JSON
            StringBuilder json = new StringBuilder("[");
            
            //Y vamos referenciando cada elemento que nos traiga el metodo de listarPrendasBajasStock
            for (int i = 0; i < bajoStock.size(); i++) {
                
                Prendas item = bajoStock.get(i);
                
                json.append("{");
                json.append("\"id\":").append(item.getId()).append(",");
                json.append("\"nombre\":\"").append(escaparJSON(item.getNombre())).append("\",");
                json.append("\"precio\":").append(item.getValor()).append(","); 
                json.append("\"stock\":").append(item.getStock()).append(",");
                json.append("\"talla\":\"").append(item.getTalla()).append("\",");
                json.append("\"imagen\":\"").append(escaparJSON(item.getImagen())).append("\""); 
                json.append("}");
                
                if (i < bajoStock.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        }
        
        //Fuerza una salida limpiando el canal de salida.
        out.flush();
    } // <- Aquí se cierra correctamente el método doGet

    //Metodo auxiliar que blinda las los json en caso de que pasen
    //un descripcion o algo con ""
    private String escaparJSON(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}



