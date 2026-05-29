
    package dao;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.*;
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
                    prenda.setCategoria(rs.getString("Categoria_nombre"));
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
        
        // Se crea un nuevo metodo para obtener las categorias sin repetirlas
        public List<Map<String, String>> listarCategorias(){
            
            List<Map<String, String>> categorias = new ArrayList<>();
            // Se hace la peticion a la tabla categorias para que me traiga las categorias disponibles
            String sql = "SELECT Categoria_id, Categoria_nombre FROM Categoria ORDER BY Categoria_nombre ASC;";
           
            // Ejecucion de la varible sql
            try (Connection con = ClaseConexion.getConexion();
                PreparedStatement pd = con.prepareStatement(sql);
                ResultSet rs = pd.executeQuery()){
            
                while (rs.next()){
                    Map<String, String> categoria = new HashMap<>();
                    categoria.put("id", String.valueOf(rs.getInt("Categoria_id")));
                    categoria.put("nombre", rs.getString("Categoria_nombre"));
                    categorias.add(categoria);
                }
            
            }
            catch (SQLException e){
                System.out.println("Error al listar desde la tabla Categoria: " + e.getMessage()); 
            }
            
            return categorias;
        }
        
        public boolean actualizarPrenda(int idPrenda, String nombre, double valor, String talla, int idCategoria, int stock, String estado, String descripcion) {
            // Sentencia SQL utilizando la relación FK de tu tabla Categoria
            String sql = "UPDATE Prendas SET Prenda_nombre = ?, Prenda_valor = ?, Prenda_talla = ?, "
               + "Categoria_id = ?, Prenda_stock = ?, Prenda_estado = ?, Prenda_descripcion = ? "
               + "WHERE Prenda_id = ?;";
               
            try (Connection con = ClaseConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
        
                // Inyectamos las variables en el orden exacto de los signos de interrogación
                ps.setString(1, nombre);
                ps.setDouble(2, valor);
                ps.setString(3, talla);
                ps.setInt(4, idCategoria); // La FK numérica de la categoría elegida
                ps.setInt(5, stock);
                ps.setString(6, estado);    // "activa" o "inactiva" según validó el JS
                ps.setString(7, descripcion);
                ps.setInt(8, idPrenda);     // El identificador para el WHERE
        
                // Devuelve true si se modificó la fila exitosamente
                return ps.executeUpdate() > 0;
        
            } 
            catch (SQLException e) {
                System.out.println("Error al realizar el UPDATE relacional en PrendasDAO: " + e.getMessage());
                return false;
            }
        }
        
        public void sincronizarImagenesPrenda(int idPrenda, java.util.List<String> listaRutas) {
            // 1. Sentencia para limpiar el historial de imágenes de esta prenda en específico
            String sqlDelete = "DELETE FROM imagenes WHERE Prenda_id = ?;"; // Ajusta 'Prendas_id' al nombre exacto de tu FK
    
            // 2. Sentencia para insertar las imágenes que quedaron vigentes
            String sqlInsert = "INSERT INTO imagenes (Imagenes_link, Prenda_id) VALUES (?, ?);"; // Ajusta columnas si cambian
    
            try (Connection con = ClaseConexion.getConexion()) {
                // Desactivamos el autocommit para controlar la transacción manualmente (Seguridad ante todo)
                con.setAutoCommit(false);
        
                try (PreparedStatement psDelete = con.prepareStatement(sqlDelete);
                    PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
            
                    // Realizamos un borrado preventivo
                    psDelete.setInt(1, idPrenda);
                    psDelete.executeUpdate();
            
                    if (listaRutas != null && !listaRutas.isEmpty()) {
                       
                        // Se Realiza una insercion en masa
                        for (String ruta : listaRutas) {
                            if (!ruta.trim().isEmpty()) {
                                psInsert.setString(1, ruta.trim());
                                psInsert.setInt(2, idPrenda);
                                psInsert.addBatch(); // Se acumula en el lote de ejecución
                            }
                        }
                
                        // Ejecutamos todo el lote de inserciones junto
                        psInsert.executeBatch();
                    }
            
                    // Si todo salió perfecto, guardamos los cambios definitivamente en MySQL
                    con.commit();
                    System.out.println("Sincronización de imágenes realizada con éxito para la prenda ID: " + idPrenda);
                
                } 
                catch (SQLException e) {
                    // En caso de que algo llegue a fallar
                    con.rollback();
                    System.out.println("Error en la transacción de imágenes. Se aplicó Rollback: " + e.getMessage());
                }
        
            } 
            catch (SQLException e) {
                System.out.println("Error de conexión al sincronizar imágenes en PrendasDAO: " + e.getMessage());
            }
        }
    }
