package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import modelo.Prendas;

public class PrendasDAO {

    // =========================================================================
    // 1. CONSULTAS Y LECTURAS (Catálogo y Detalles)
    // =========================================================================

    /**
     * Lista las prendas agrupadas por nombre para el catálogo del CLIENTE.
     * (No muestra agotados, inactivos ni eliminados)
     */
    public List<Prendas> listarPrendas() {
        List<Prendas> listaProductos = new ArrayList<>();
        String sql = "SELECT " +
                     "  MIN(p.Prenda_id) as Prenda_id, " + 
                     "  p.Prenda_nombre, " +
                     "  p.Prenda_tipo, " +
                     "  MIN(p.Prenda_valor) as Prenda_valor, " + 
                     "  p.Prenda_descripcion, " +
                     "  SUM(p.Prenda_stock) as Prenda_stock, " + 
                     "  p.Prenda_estado, " +
                     "  c.Categoria_nombre, " +
                     "  MAX(i.Imagenes_link) as Imagenes_link, " + 
                     "  SUM(IFNULL(pop.Populares_visitas, 0)) as visitas, " + 
                     "  GROUP_CONCAT(DISTINCT p.Prenda_talla ORDER BY p.Prenda_talla SEPARATOR ', ') as Prenda_talla " + 
                     "FROM Prendas p " +
                     "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " +
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "LEFT JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                     "WHERE p.Prenda_stock > 0 AND p.Prenda_estado = 'activa' " +
                     "GROUP BY p.Prenda_nombre, p.Prenda_tipo, p.Prenda_descripcion, p.Prenda_estado, c.Categoria_nombre " +
                     "ORDER BY visitas DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaProductos;
    }

    /**
     * 🌟 [NUEVO MÉTODO] Lista las prendas de forma individual para el ADMINISTRADOR.
     * Muestra productos activos e inactivos, pero oculta los que están en estado 'eliminada'.
     */
    public List<Prendas> listarPrendasAdmin() {
        List<Prendas> listaProductos = new ArrayList<>();
        String sql = "SELECT p.Prenda_id, p.Prenda_nombre, p.Prenda_tipo, p.Prenda_valor, " +
                     "p.Prenda_talla, p.Prenda_descripcion, p.Prenda_stock, p.Prenda_estado, " +
                     "c.Categoria_nombre, MAX(i.Imagenes_link) as Imagenes_link " +
                     "FROM Prendas p " +
                     "JOIN Categoria c ON p.Categoria_id = c.Categoria_id " +
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "WHERE p.Prenda_estado != 'eliminada' " + // Usando el índice idx_prendas_gestion
                     "GROUP BY p.Prenda_id, p.Prenda_nombre, p.Prenda_tipo, p.Prenda_valor, p.Prenda_talla, p.Prenda_descripcion, p.Prenda_stock, p.Prenda_estado, c.Categoria_nombre " +
                     "ORDER BY p.Prenda_id DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
                listaProductos.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Error en listarPrendasAdmin: " + e.getMessage());
        }
        return listaProductos;
    }

    /**
     * Obtiene las categorías disponibles sin repetir para filtros e inputs.
     */
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
        } catch (SQLException e) {
            System.out.println("Error al listar Categorias: " + e.getMessage());
        }
        return categorias;
    }

    /**
     * Trae la información unificada de una prenda base y desglosa todas sus variantes de tallas.
     */
    public Map<String, Object> obtenerDetallesPrendaConVariantes(int idReferencia) {
        Map<String, Object> resultado = new java.util.LinkedHashMap<>();
        java.util.Set<String> listaImagenes = new java.util.LinkedHashSet<>();
        java.util.Map<Integer, Map<String, Object>> variantesMap = new java.util.LinkedHashMap<>();
        
        // 🌟 [CAMBIO BORRADO LÓGICO]: Cambiado p2.Prenda_estado = 'activa' por != 'eliminada' 
        // para permitir editar prendas inactivas. Agregado c.Categoria_id al SELECT.
        String sql = "SELECT p2.Prenda_id, p2.Prenda_nombre, p2.Prenda_descripcion, p2.Prenda_tipo, " +
                     "       p2.Prenda_talla, p2.Prenda_stock, p2.Prenda_valor, p2.Categoria_id, i.Imagenes_link " +
                     "FROM Prendas p1 " +
                     "JOIN Prendas p2 ON p1.Prenda_nombre = p2.Prenda_nombre " +
                     "LEFT JOIN imagenes i ON p2.Prenda_id = i.Prenda_id " +
                     "WHERE p1.Prenda_id = ? AND p2.Prenda_estado != 'eliminada'";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idReferencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!resultado.containsKey("nombre")) {
                        resultado.put("nombre", rs.getString("Prenda_nombre"));
                        resultado.put("descripcion", rs.getString("Prenda_descripcion"));
                        resultado.put("tipo", rs.getString("Prenda_tipo"));
                        resultado.put("categoriaId", rs.getInt("Categoria_id"));
                    }
                    
                    String imgLink = rs.getString("Imagenes_link");
                    if (imgLink != null && !imgLink.isEmpty()) {
                        listaImagenes.add(imgLink);
                    }
                    
                    int varianteId = rs.getInt("Prenda_id");
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
            if (!resultado.isEmpty()) {
                resultado.put("listaImagenes", new java.util.ArrayList<>(listaImagenes));
                resultado.put("variantes", new java.util.ArrayList<>(variantesMap.values()));
            }
        } catch (Exception e) {
            System.out.println("Error en obtenerDetallesPrendaConVariantes: " + e.getMessage());
        }
        return resultado.isEmpty() ? null : resultado;
    }

    // =========================================================================
    // 2. HELPERS PRIVADOS (Mantenimiento de Código Limpio / DRY)
    // =========================================================================

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

    // =========================================================================
    // 3. OPERACIONES DEL ADMINISTRADOR (Adaptadas a Variantes/Tallajes)
    // =========================================================================

    public boolean registrarProductoConVariantes(String nombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas) {
        String sqlPrenda = "INSERT INTO Prendas (Prenda_nombre, Prenda_tipo, Prenda_valor, Prenda_talla, Categoria_id, Prenda_stock, Prenda_estado, Prenda_descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        Connection con = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); 
            
            //Con.setAutoCommit: funciona como un respaldo en caso de que algo salga mal ya que me hara un rollback
            //Si alguna ejecucion sale mal o si todo funciona el codigo continua normalmente

            try (PreparedStatement ps = con.prepareStatement(sqlPrenda, Statement.RETURN_GENERATED_KEYS)) {
                int idPrimerVariante = -1;

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

                int[] resultadoBatch = ps.executeBatch();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPrimerVariante = rs.getInt(1);
                    }
                }

                if (resultadoBatch.length > 0 && idPrimerVariante != -1) {
                    guardarImagenesBatch(idPrimerVariante, listaRutas, con);
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                if (con != null) con.rollback();
                System.out.println("Error al procesar lote de variantes: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión en registrarProducto: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean actualizarProductoConVariantes(String nombreOriginal, String nuevoNombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas, int idRepresentativo) {
        Connection con = null;
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false);

            // Actualizacion Global
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

            
            List<Integer> idsEntrantes = new ArrayList<>();
            for (Map<String, Object> var : variantes) {
                if (var.containsKey("id") && var.get("id") != null) {
                    idsEntrantes.add(((Number) var.get("id")).intValue());
                }
            }

            // Se cambió 'activa' por != 'eliminada' para no omitir variantes inactivas
            String sqlBuscarActuales = "SELECT Prenda_id FROM Prendas WHERE Prenda_nombre = ? AND Prenda_estado != 'eliminada';";
            List<Integer> idsEnBD = new ArrayList<>();
            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscarActuales)) {
                psBuscar.setString(1, nuevoNombre);
                try (ResultSet rs = psBuscar.executeQuery()) {
                    while (rs.next()) {
                        idsEnBD.add(rs.getInt("Prenda_id"));
                    }
                }
            }

            // Si una variante se borra desde el formulario de edición,
            // pasa al estado 'eliminada' para mantener la consistencia con el borrado masivo.
            String sqlDesactivarVar = "UPDATE Prendas SET Prenda_estado = 'eliminada' WHERE Prenda_id = ?;";
            try (PreparedStatement psDesactivar = con.prepareStatement(sqlDesactivarVar)) {
                for (int idBD : idsEnBD) {
                    if (!idsEntrantes.contains(idBD)) {
                        psDesactivar.setInt(1, idBD);
                        psDesactivar.addBatch();
                        
                        //El add batch funciona de tal foma de que si el usuario quiere comprar
                        //10 camisas 
                    }
                }
                psDesactivar.executeBatch();
            }

            // Procesamiento de varientes esto funciona para manejar tallajes
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

                    if (var.containsKey("id") && var.get("id") != null) {
                        int idVar = ((Number) var.get("id")).intValue();
                        psUp.setString(1, talla);
                        psUp.setInt(2, stock);
                        psUp.setDouble(3, valor);
                        psUp.setString(4, estado); 
                        psUp.setInt(5, idVar);
                        psUp.addBatch();
                        tieneExistentes = true;
                    } else {
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

                if (tieneExistentes) psUp.executeBatch();
                if (tieneNuevas) psIns.executeBatch();
            }

            // 4. IMÁGENES
            guardarImagenesBatch(idRepresentativo, listaRutas, con);

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Error en la transacción de actualización: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Aplica un borrado lógico (eliminada) a todas las variantes asociadas a un nombre.
     *  Cambiado 'inactiva' por 'eliminada'.
     */
    public boolean desactivarProductoCompleto(String nombrePrenda) {
        String sql = "UPDATE Prendas SET Prenda_estado = 'eliminada' WHERE Prenda_nombre = ?;";
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nombrePrenda);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al desactivar el producto: " + e.getMessage());
            return false;
        }
    }
}
