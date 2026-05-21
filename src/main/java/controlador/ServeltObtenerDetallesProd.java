
package controlador;

import dao.PrendasDAO;
import com.google.gson.Gson;
import modelo.Prendas;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

    @WebServlet("/ObtenerProductosDetalle")
    public class ServeltObtenerDetallesProd extends HttpServlet{
    
        //Para que que le metodo pueda procesar de manera correcta el doGet
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException{
        
            //Transformamos los elementos a un json
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            //Caturamos el id que viene de enlace(URL)
            String idParam = request.getParameter("id");
            
            try(PrintWriter out = response.getWriter()){
            
                if(idParam == null || idParam.isEmpty()){
                
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el id del producto");
                    return;
                }
                
                int id = Integer.parseInt(idParam);
                
                //Llamamos al dao para obtener los datos de la prenda
                PrendasDAO dao = new PrendasDAO();
                Prendas prenda = dao.obtenerPorId(id);
                
                if( prenda != null){
                    
                    //convertir el objecto a json con ayuda de GSON
                    Gson gson = new Gson();
                    String json = gson.toJson(prenda);
                    out.print(json);
                }
                else{
                    //En caso de que no existe el id
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado");
                }
            }
            //En caso de que el dato ingresado no sea de tipo numerico
            catch (NumberFormatException e){
                
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID no valido");
            }
        }
    }

