
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
                p.setVisitas(rs.getInt("visitas"));
                
                listaProductos.add(p);
            }
            
            System.out.println("DEBUG: Tamaño de la lista enviada: " + listaProductos.size());
        }
        
        
        catch (SQLException e){
            e.printStackTrace( );
        }
        
        return listaProductos;
    }
    
    public Prendas obtenerPorId(int id){
        
        Prendas prenda = null;
        
        String sql = "SELECT p.*, c.Categoria_nombre, " +
             "(SELECT i.Imagenes_link FROM imagenes i WHERE i.Prenda_id = p.Prenda_id LIMIT 1) as Imagenes_link " +
             "FROM Prendas p " +
             "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " +
             "WHERE p.Prenda_id = ?";
        
        try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setInt(1, id);
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                prenda = new Prendas();
                // 3. Mapeo exhaustivo (todos los campos de tu clase)
                prenda.setId(rs.getInt("Prenda_id"));
                prenda.setNombre(rs.getString("Prenda_nombre"));
                prenda.setDescripcion(rs.getString("Prenda_descripcion"));
                prenda.setValor(rs.getDouble("Prenda_valor"));
                prenda.setTalla(rs.getString("Prenda_talla"));
                prenda.setEstado(rs.getString("Prenda_estado"));
                prenda.setImagen(rs.getString("Imagenes_link"));
                prenda.setVisitas(rs.getInt("Prenda_visitas"));
                
                System.out.println(prenda);
                
            }
        }
        
        } catch (SQLException e) {
            System.err.println("Error al obtener prenda por ID: " + e.getMessage());
         }
    
        return prenda;
    }
}
