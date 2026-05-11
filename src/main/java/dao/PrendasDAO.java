
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Prendas;
import dao.ClaseConexion;
import java.util.List;

public class PrendasDAO {
    
    public List<Prendas> listarPrendas() {
       
        List<Prendas> listaProductos = new ArrayList<>();
        
        String sql = "SELECT p.*, c.Categoria_nombre, max(i.Imagenes_link) as Imagenes_link, IFNULL(pop.Populares_visitas, 0) as visitas " +
                 "FROM Prendas p " +  
                 "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " + 
                 "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                 "LEFT JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                 "GROUP BY p.Prenda_id, c.Categoria_nombre, pop.Populares_visitas " +
                 "ORDER BY visitas DESC";
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()){
            
            while (rs.next()){
      
                Prendas p = new Prendas();
                p.setId(rs.getInt("Prenda_id") );
                p.setNombre(rs.getString("Prenda_nombre"));
                p.setTipoPrenda(rs.getString("Prenda_tipo"));
                p.setValor(rs.getDouble("Prenda_valor"));
                p.setTalla(rs.getString("Prenda_talla"));
                p.setDescripcion(rs.getString("Prenda_descripcion"));
                p.setStock(rs.getInt("Prenda_stock"));
                p.setEstado(rs.getString("Prenda_estado"));
                p.setCategoria(rs.getString("Categoria_nombre"));
                p.setImagen(rs.getString("Imagenes_link"));
                p.setVisitas(rs.getInt("Populares_visitas"));
                
                listaProductos.add(p);
            }
        }
        
        catch (SQLException e){
            System.out.println("Hubo algun error al momento de obtener las prendas" );
        }
        
        return listaProductos;
    
    }
    
}
