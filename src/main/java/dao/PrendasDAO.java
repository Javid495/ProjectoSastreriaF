
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
                 System.out.println(con);

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

            String sql = "SELECT p.*, c.Categoria_nombre " +
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
                    
                    //Consulta para traerme todas las imagenes asociadas a un producto
                    String sqlImgs = "select Imagenes_link from imagenes where Prenda_id = ?";
                    
                    try (PreparedStatement psImgs = con.prepareStatement(sqlImgs)){
                        
                        psImgs.setInt(1, id);
                        
                        try (ResultSet rsImgs = psImgs.executeQuery()){
                            //Creo mi array de imagenes
                            List<String> imgs = new ArrayList<>();
                            
                            //vOy almacenando cada imagen de la prenda
                            while (rsImgs.next()){
                                imgs.add(rsImgs.getString("Imagenes_link"));
                            }
                            
                            if (!imgs.isEmpty()) {
                                prenda.setImagen(imgs.get(0));
                            }
                            
                            //y guardo el array con las imagenes
                            prenda.setListaImagenes(imgs);
                        }
                    }
                    System.out.println(prenda);
                    System.out.println("mapeo realizado con exito");

                }
            }

            } catch (SQLException e) {
                System.err.println("Error al obtener prenda por ID: " + e.getMessage());
             }

            return prenda;
        }
}
