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
     * Lista las prendas agrupadas por nombre para el catálogo del cliente.
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
        
        String sql = "SELECT p2.Prenda_id, p2.Prenda_nombre, p2.Prenda_descripcion, p2.Prenda_tipo, " +
                     "       p2.Prenda_talla, p2.Prenda_stock, p2.Prenda_valor, i.Imagenes_link " +
                     "FROM Prendas p1 " +
                     "JOIN Prendas p2 ON p1.Prenda_nombre = p2.Prenda_nombre " +
                     "LEFT JOIN imagenes i ON p2.Prenda_id = i.Prenda_id " +
                     "WHERE p1.Prenda_id = ? AND p2.Prenda_estado = 'activa'";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idReferencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!resultado.containsKey("nombre")) {
                        resultado.put("nombre", rs.getString("Prenda_nombre"));
                        resultado.put("descripcion", rs.getString("Prenda_descripcion"));
                        resultado.put("tipo", rs.getString("Prenda_tipo"));
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

    /**
     * ÚNICO método encargado de gestionar las imágenes. Elimina código duplicado.
     * Reutiliza una conexión activa para integrarse a transacciones complejas.
     */
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

    /**
     * Inserta un nuevo producto creando una fila independiente por cada variante de talla.
     */
    public boolean registrarProductoConVariantes(String nombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas) {
        String sqlPrenda = "INSERT INTO Prendas (Prenda_nombre, Prenda_tipo, Prenda_valor, Prenda_talla, Categoria_id, Prenda_stock, Prenda_estado, Prenda_descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        Connection con = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // Garantiza una transacción atómica

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
                        idPrimerVariante = rs.getInt(1); // Recupera el ID para asociar las imágenes
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

    /**
     * Actualiza los datos compartidos globalmente y procesa el inventario de las variantes.
     */
    public boolean actualizarProductoConVariantes(String nombreOriginal, String nuevoNombre, String tipo, int idCategoria, String estado, String descripcion, List<Map<String, Object>> variantes, List<String> listaRutas, int idRepresentativo) {
        Connection con = null;
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false);

            // 1. Actualizar los datos comunes de todas las variantes que compartían el nombre anterior
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

            // 2. Sincronizar las variantes individuales (Actualizar stock/valor de las existentes)
            String sqlVariante = "UPDATE Prendas SET Prenda_stock = ?, Prenda_valor = ? WHERE Prenda_id = ?;";
            try (PreparedStatement psVar = con.prepareStatement(sqlVariante)) {
                for (Map<String, Object> var : variantes) {
                    if (var.containsKey("id")) { // Si tiene ID, es una variante ya existente en DB
                        psVar.setInt(1, ((Number) var.get("stock")).intValue());
                        psVar.setDouble(2, ((Number) var.get("valor")).doubleValue());
                        psVar.setInt(3, ((Number) var.get("id")).intValue());
                        psVar.addBatch();
                    }
                }
                psVar.executeBatch();
            }

            // 3. Sincronizar lote de imágenes utilizando el helper unificado
            guardarImagenesBatch(idRepresentativo, listaRutas, con);

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.out.println("Error en la transacción de actualización: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * Aplica un borrado lógico (inactiva) a todas las variantes asociadas a un nombre.
     */
    public boolean desactivarProductoCompleto(String nombrePrenda) {
        String sql = "UPDATE Prendas SET Prenda_estado = 'inactiva' WHERE Prenda_nombre = ?;";
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
