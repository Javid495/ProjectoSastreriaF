package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import Dtos.Prendas;

public class PrendasDAO {

    // 1. CONSULTAS Y LECTURAS (Catálogo y Detalles)

    // Lista las prendas agrupadas por nombre para el catálogo de parte del cliente.
    public List<Prendas> listarPrendas() {
        List<Prendas> listaProductos = new ArrayList<>();
        
        // Consulta que me verifica si el Nombre, el tipo de prenda, el estado y categoria son iguales en caso de se asi la incluye como parte de la misma prenda y la junta
        
        String sql = "SELECT " +
                     "  MIN(p.Prenda_id) as Prenda_id, " + 
                     "  p.Prenda_nombre, " +
                     "  p.Prenda_tipo, " +
                     "  MIN(p.Prenda_valor) as Prenda_valor, " + 
                     "  MAX(p.Prenda_descripcion) as Prenda_descripcion, " + // <-- Evita duplicar si varían las descripciones
                     "  SUM(p.Prenda_stock) as Prenda_stock, " + 
                     "  p.Prenda_estado, " +
                     "  c.Categoria_nombre, " +
                     "  MAX(i.Imagenes_link) as Imagenes_link, " + 
                     "  SUM(IFNULL(pop.Populares_visitas, 0)) as visitas, " + 
                     //Revisa tallas repetidas
                     "  GROUP_CONCAT(DISTINCT p.Prenda_talla ORDER BY p.Prenda_talla SEPARATOR ', ') as Prenda_talla " + 
                     //La tabla base de donde hago la consulta datos de la consulta de la prenda 
                     "FROM Prendas p " +
                     // Que me junte o verifique si la prenda tiene un categoria asociada
                     "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " +
                     //Se hace un left join por seguridad si una prenda es nueva pero no tiene imagen o visitas
                     //el producto sale en el catalogo
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "LEFT JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                     // que me haga la consulta para prendas que tengan un stock superior a 0 y que esten activas
                     "WHERE p.Prenda_stock > 0 AND p.Prenda_estado = 'activa' " +
                     //En dado caso compratan el nombre. tipo, estado y categoria me lo agurpa en una fila
                     "GROUP BY p.Prenda_nombre, p.Prenda_tipo, p.Prenda_estado, c.Categoria_nombre " +
                     //Y las ordena de manera decendente
                     "ORDER BY visitas DESC";
        
        //Preparamos la conexion con la base de datos
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            //Traemos los datos de la base de datos
            while (rs.next()) {
                Prendas p = new Prendas();
                p.setId(rs.getInt("Prenda_id"));
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
        } 
        
        //En caso de que suceda algun error en la base de datos
        catch (SQLException e) {
            //Verifica los errores de la consulta sql
            e.printStackTrace();
        }
        
        //Retorno la lista de productos
        return listaProductos;
    }


    // Clase que me muestra el listado de prendas para el administrador (Sin duplicados por variante)
   public List<Prendas> listarPrendasAdmin() {
       List<Prendas> listaProductos = new ArrayList<>();

       // Agrupamos por nombre/tipo/estado y consolidamos las variantes (IDs, stocks y tallas)
       // OBSV: aunque tecnicamente esta haciendo la misma funcion que el metodo listar prendas
       // La onsulta cambia en un par de factores siendo que no se tiene en cuenta los populares y 
       // Se muestran las prendas que tengan stock en 0.
       String sql = "SELECT " +
                    "  MIN(p.Prenda_id) as Prenda_id, " +
                    "  p.Prenda_nombre, " +
                    "  p.Prenda_tipo, " +
                    "  MIN(p.Prenda_valor) as Prenda_valor, " +
                    "  GROUP_CONCAT(DISTINCT p.Prenda_talla ORDER BY p.Prenda_talla SEPARATOR ', ') as Prenda_talla, " +
                    "  MAX(p.Prenda_descripcion) as Prenda_descripcion, " +
                    "  SUM(p.Prenda_stock) as Prenda_stock, " +
                    "  p.Prenda_estado, " +
                    "  c.Categoria_nombre, " +
                    "  MAX(i.Imagenes_link) as Imagenes_link " +
                    "FROM Prendas p " +
                    "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " +
                    "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                    "WHERE p.Prenda_estado != 'eliminada' " + 
                    "GROUP BY p.Prenda_nombre, p.Prenda_tipo, p.Prenda_estado, c.Categoria_nombre " +
                    "ORDER BY Prenda_id DESC";

       // Realizamos la conexion con la base de datos
       try (Connection con = ClaseConexion.getConexion();
            //Preparamos la consulta para ser ejecutada
            PreparedStatement ps = con.prepareStatement(sql);
            //Ejecutamos la consulta
            ResultSet rs = ps.executeQuery()) {

           //Traemos lo datos de la base de datos
           while (rs.next()) {
               Prendas p = new Prendas();
               p.setId(rs.getInt("Prenda_id")); // Será el ID más bajo de sus variantes (sirve de referencia para editar)
               p.setNombre(rs.getString("Prenda_nombre"));
               p.setTipoPrenda(rs.getString("Prenda_tipo"));
               p.setValor(rs.getDouble("Prenda_valor"));
               p.setTalla(rs.getString("Prenda_talla")); // Devolverá algo como "S, M" en lugar de duplicar la tarjeta
               p.setDescripcion(rs.getString("Prenda_descripcion"));
               p.setStock(rs.getInt("Prenda_stock")); // Muestra la suma total del inventario de todas las tallas
               p.setEstado(rs.getString("Prenda_estado"));
               p.setCategoria(rs.getString("Categoria_nombre"));
               p.setImagen(rs.getString("Imagenes_link"));
               listaProductos.add(p);
           }
       } 
       //En caso de que suceda algun error
       catch (SQLException e) {
           System.out.println("Error en listarPrendasAdmin: " + e.getMessage());
       }

       //Retorno la lista de productos con sus variantes unificadas
       return listaProductos;
   }

    //Se obtiene la lista de categorias que estan disponible de la base de datos
    public List<Map<String, String>> listarCategorias() {
        List<Map<String, String>> categorias = new ArrayList<>();
        String sql = "SELECT Categoria_id, Categoria_nombre FROM Categoria ORDER BY Categoria_nombre ASC;";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement pd = con.prepareStatement(sql);
             ResultSet rs = pd.executeQuery()) {

            while (rs.next()) {
                Map<String, String> categoria = new HashMap<>();
                categoria.put("id", String.valueOf(rs.getInt("Categoria_id")));
                categoria.put("nombre", rs.getString("Categoria_nombre"));
                categorias.add(categoria);
            }
        } 
        
        //Si exite  algun error de parte de mysql
        catch (SQLException e) {
            System.out.println("Error al listar Categorias: " + e.getMessage());
        }
        
        //Retorno mi lista de categorias
        return categorias;
    }

    /**
     * Trae la información unificada de una prenda base y desglosa sus variantes.
     */
    
    public Map<String, Object> obtenerDetallesPrendaConVariantes(int idReferencia) {
        
        //Contnedor de elementos que tendra el resultado final de la consulta
        Map<String, Object> resultado = new java.util.LinkedHashMap<>();
        
        //Contenedor que listara las imagenes asegurandose de que no hayan repetidas
        java.util.Set<String> listaImagenes = new java.util.LinkedHashSet<>();
        
        //Un mapeo temporal de los datos de tallejes de la prenda
        java.util.Map<Integer, Map<String, Object>> variantesMap = new java.util.LinkedHashMap<>();
        
        //Consulta sql para consultar los variantes de las prendas
        String sql = "SELECT p2.Prenda_id, p2.Prenda_nombre, p2.Prenda_descripcion, p2.Prenda_tipo, " +
                     "       p2.Prenda_talla, p2.Prenda_stock, p2.Prenda_valor, p2.Categoria_id, i.Imagenes_link " +
                     "FROM Prendas p1 " +
                     "JOIN Prendas p2 ON p1.Prenda_nombre = p2.Prenda_nombre " +
                     "LEFT JOIN imagenes i ON p2.Prenda_id = i.Prenda_id " +
                     "WHERE p1.Prenda_id = ? AND p2.Prenda_estado != 'eliminada'";

        try (Connection con = ClaseConexion.getConexion();
                
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            //Id o referencia con la cual ejecuto la consulta (es decir el dato que ocupa el ?) o id cabeza de la prenda
            ps.setInt(1, idReferencia);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    
                    if (!resultado.containsKey("nombre")) {
                        
                        resultado.put("nombre", rs.getString("Prenda_nombre"));
                        resultado.put("descripcion", rs.getString("Prenda_descripcion")); // Mapea la primera prenda sin problema
                        resultado.put("tipo", rs.getString("Prenda_tipo"));
                        resultado.put("categoriaId", rs.getInt("Categoria_id"));
                    }
                    
                    String imgLink = rs.getString("Imagenes_link");
                    
                    if (imgLink != null && !imgLink.isEmpty()) {
                        listaImagenes.add(imgLink);
                    }
                    
                    int varianteId = rs.getInt("Prenda_id");
                    
                    //Validacion que evita la duplicidad de elementos
                    if (!variantesMap.containsKey(varianteId)) {
                        
                        Map<String, Object> variante = new java.util.LinkedHashMap<>();
                        
                        variante.put("id", varianteId);
                        variante.put("talla", rs.getString("Prenda_talla"));
                        variante.put("stock", rs.getInt("Prenda_stock"));
                        variante.put("valor", rs.getDouble("Prenda_valor"));
                        variantesMap.put(varianteId, variante);
                    }
                }
            }
            
            //Etruturamos las imagenes y variantes(Tallajes en la lista principal)
            if (!resultado.isEmpty()) {
                
                resultado.put("listaImagenes", new java.util.ArrayList<>(listaImagenes));
                resultado.put("variantes", new java.util.ArrayList<>(variantesMap.values()));
            }
        } 
        
        catch (Exception e) {
            System.out.println("Error en obtenerDetallesPrendaConVariantes: " + e.getMessage());
        }
        
        return resultado.isEmpty() ? null : resultado;
    }

    private void guardarImagenesBatch(int idPrenda, List<String> listaRutas, Connection con) throws SQLException {
        String sqlDelete = "DELETE FROM imagenes WHERE Prenda_id = ?;";
        String sqlInsert = "INSERT INTO imagenes (Imagenes_link, Prenda_id) VALUES (?, ?);";

        try (PreparedStatement psDelete = con.prepareStatement(sqlDelete);
             PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
            
            psDelete.setInt(1, idPrenda);
            psDelete.executeUpdate();

            if (listaRutas != null && !listaRutas.isEmpty()) {
                for (String ruta : listaRutas) {
                    if (ruta != null && !ruta.trim().isEmpty()) {
                        psInsert.setString(1, ruta.trim());
                        psInsert.setInt(2, idPrenda);
                        psInsert.addBatch();
                    }
                }
                psInsert.executeBatch();
            }
        }
    }

    //Operaciones de Administrador de agregar prendas y tallajes

    //Clase de registrar productos con variantes, recibe los datos que vienen del frontend
    public boolean registrarProductoConVariantes(String nombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas) {
        
        // preparo la ejecucion del insert o codigo a la basse de datos
        String sqlPrenda = "INSERT INTO Prendas (Prenda_nombre, Prenda_tipo, Prenda_valor, Prenda_talla, Categoria_id, Prenda_stock, Prenda_estado, Prenda_descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        
        //Declaro mi variable conexion
        Connection con = null;
        
        // Un try en caso de algun error
        try {
            
            // Instancio mi archivo conexion para hacer conexion a la base de datos
            con = ClaseConexion.getConexion();
            
            //Desactivo el guardado automatico para hacer un checkpoint en caso de errores
            con.setAutoCommit(false); 
            
            //Preparo la conexion con la base de datos, y le pido que me traiga el primer id que me genere
            try (PreparedStatement ps = con.prepareStatement(sqlPrenda, Statement.RETURN_GENERATED_KEYS)) {
                int idPrimerVariante = -1;

                //Mapeo los datos de la prenda con sus variantes
                for (Map<String, Object> variante : variantes) {
                    ps.setString(1, nombre);
                    ps.setString(2, tipo);
                    ps.setDouble(3, ((Number) variante.get("valor")).doubleValue());
                    ps.setString(4, (String) variante.get("talla"));
                    ps.setInt(5, idCategoria);
                    ps.setInt(6, ((Number) variante.get("stock")).intValue());
                    ps.setString(7, estado);
                    ps.setString(8, descripcion);
                    ps.addBatch();
                }

                //Ejecuto la insecion
                int[] resultadoBatch = ps.executeBatch();

                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPrimerVariante = rs.getInt(1);
                    }
                }

                //Condicional que verifica que si al menos hay una varianri y si me trajo el id generado de la base de datos
                if (resultadoBatch.length > 0 && idPrimerVariante != -1) {
                    
                    //y guardo las imagenes referenceadas con el id de la prenda
                    guardarImagenesBatch(idPrimerVariante, listaRutas, con);
                }

                //si hay exito y guardo el cambio
                con.commit();
                return true;
            } 
            
            catch (SQLException e) {
                
                //Si sucede algun error hago un rollback que me retorne al checkpoint o guardado anteriror
                if (con != null) con.rollback();
                System.out.println("Error al procesar lote de variantes: " + e.getMessage());
                return false;
            }
        } 
        
        //Si sucede algun error en la ejcucion del sql  o script de la base de datos
        catch (SQLException e) {
            System.out.println("Error de conexión en registrarProducto: " + e.getMessage());
            return false;
        } 
        
        finally {
            
            if (con != null) {
                try { con.close(); } 
                catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Método principal para actualizar un producto y sincronizar de forma masiva sus variantes (tallas) e imágenes
    public boolean actualizarProductoConVariantes(String nombreOriginal, String nuevoNombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas, int idRepresentativo) {

        Connection con = null;

        try {
            con = ClaseConexion.getConexion();
            
            // PASO 1: Desactivamos el autocommit para manejar todo como una única Transacción Segura
            con.setAutoCommit(false);

            // PASO 2: Actualización de campos globales para todas las prendas que comparten el mismo nombre antiguo
            String sqlGlobal = "UPDATE Prendas SET Prenda_nombre = ?, Prenda_tipo = ?, Categoria_id = ?, Prenda_estado = ?, Prenda_descripcion = ? WHERE Prenda_nombre = ?;";
            
            try (PreparedStatement psGlobal = con.prepareStatement(sqlGlobal)) {
                psGlobal.setString(1, nuevoNombre);
                psGlobal.setString(2, tipo);
                psGlobal.setInt(3, idCategoria);
                psGlobal.setString(4, estado);
                psGlobal.setString(5, descripcion);
                psGlobal.setString(6, nombreOriginal);
                psGlobal.executeUpdate();
            }

                // Identificar qué IDs se recibieron
                //Creamos una lista que va almacenar los id entrantes
                List<Integer> idsEntrantes = new ArrayList<>();

                //Se revisan cada variantes que vienen de frontend
                for (Map<String, Object> var : variantes) {
                    
                    //Si la variante viene con un dato id
                    if (var.containsKey("id") && var.get("id") != null) {
                        //Vamos almacenandolo en la lista idEntrantes
                        idsEntrantes.add(((Number) var.get("id")).intValue());
                    }
                }

            // Consultar qué IDs existen actualmente en la Base de Datos para comparar
            String sqlBuscarActuales = "SELECT Prenda_id FROM Prendas WHERE Prenda_nombre = ? AND Prenda_estado != 'eliminada';";
            
            List<Integer> idsEnBD = new ArrayList<>();
            
            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscarActuales)) {
                
                psBuscar.setString(1, nuevoNombre); // Usamos nuevoNombre porque el SQL Global ya lo cambió
                
                try (ResultSet rs = psBuscar.executeQuery()) {
                    //Revisamos de esa vista de variantes que ids estan en la base de datos
                    while (rs.next()) {
                        idsEnBD.add(rs.getInt("Prenda_id"));
                    }
                }
            }

            // PASO 5: Eliminar (Desactivar) las variantes que la base de datos tiene pero que el usuario quitó en la pantalla
            String sqlDesactivarVar = "UPDATE Prendas SET Prenda_estado = 'eliminada' WHERE Prenda_id = ?;";
            
            try (PreparedStatement psDesactivar = con.prepareStatement(sqlDesactivarVar)) {
                
                for (int idBD : idsEnBD) {
                    
                    //Vamos preparando la ejecucion de cada eliminacion de las variantes
                    if (!idsEntrantes.contains(idBD)) {
                        psDesactivar.setInt(1, idBD);
                        psDesactivar.addBatch(); // Encolar la desactivación
                    }
                }
                psDesactivar.executeBatch(); // Ejecutar todas las eliminaciones juntas
            }

            // PASO 6: Preparar las estructuras para Actualizar existentes o Insertar nuevas variantes
            String sqlUpdateVariante = "UPDATE Prendas SET Prenda_talla = ?, Prenda_stock = ?, Prenda_valor = ?, Prenda_estado = ? WHERE Prenda_id = ?;";
            String sqlInsertVariante = "INSERT INTO Prendas (Prenda_nombre, Prenda_tipo, Prenda_valor, Prenda_talla, Categoria_id, Prenda_stock, Prenda_estado, Prenda_descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

            try (PreparedStatement psUp = con.prepareStatement(sqlUpdateVariante);
                 PreparedStatement psIns = con.prepareStatement(sqlInsertVariante)) {

                boolean tieneNuevas = false;
                boolean tieneExistentes = false;

                for (Map<String, Object> var : variantes) {
                    
                    String talla = (String) var.get("talla");
                    int stock = ((Number) var.get("stock")).intValue();
                    double valor = ((Number) var.get("valor")).doubleValue();

                    //Si la variante ya tiene ID, se actualizan sus datos particulares
                    if (var.containsKey("id") && var.get("id") != null) {
                        
                        int idVar = ((Number) var.get("id")).intValue();
                        psUp.setString(1, talla);
                        psUp.setInt(2, stock);
                        psUp.setDouble(3, valor);
                        psUp.setString(4, estado); 
                        psUp.setInt(5, idVar);
                        psUp.addBatch();
                        tieneExistentes = true;
                    }
                    
                    //Si la variante no tiene ID, es un registro nuevo creado por el usuario
                    else {
                        psIns.setString(1, nuevoNombre);
                        psIns.setString(2, tipo);
                        psIns.setDouble(3, valor);
                        psIns.setString(4, talla);
                        psIns.setInt(5, idCategoria);
                        psIns.setInt(6, stock);
                        psIns.setString(7, estado);
                        psIns.setString(8, descripcion);
                        psIns.addBatch();
                        tieneNuevas = true;
                    }
                }

                // Ejecutamos los lotes pendientes sólo si contienen elementos
                if (tieneExistentes) psUp.executeBatch();
                if (tieneNuevas) psIns.executeBatch();
            }

            //Guardar las rutas de las imágenes asociadas al lote de prendas
            guardarImagenesBatch(idRepresentativo, listaRutas, con);

            //Si todo el proceso se completó con éxito, consolidamos los cambios en la BD
            con.commit();
            return true;
        } 
        catch (SQLException e) {
            // En caso de cualquier error, cancelamos toda la operación para no corromper los datos
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            
            System.out.println("Error en la transacción de actualización: " + e.getMessage());
            return false;
        } 
        finally {
            // Garantizamos el cierre de la conexión pase lo que pase
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}
