package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import Dtos.Prendas;

public class PopularesDAO {

    /**
     * Registra o incrementa la visita de una prenda en caliente.
     * Registra la visita a la variante específica (talla/precio seleccionada).
     */
    public void registrarVisita(int idPrenda) {
        String sqlCheck = "SELECT COUNT(*) FROM Populares WHERE Prenda_id = ?";
        String sqlUpdate = "UPDATE Populares SET Populares_visitas = CAST(Populares_visitas AS UNSIGNED) + 1 WHERE Prenda_id = ?";
        String sqlInsert = "INSERT INTO Populares (Prenda_id, Populares_visitas) VALUES (?, '1')";

        try (Connection conn = ClaseConexion.getConexion()) {
            if (conn == null) return;

            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idPrenda);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                            psUpdate.setInt(1, idPrenda);
                            psUpdate.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                            psInsert.setInt(1, idPrenda);
                            psInsert.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trae la lista de las prendas más vistas ordenadas de mayor a menor.
     * Agrupa por atributos descriptivos compartidos para evitar la repetición de diseños en distintas tallas.
     */
    public List<Prendas> obtenerTopPopulares(int limite) {
        List<Prendas> listaPopulares = new ArrayList<>();
        
        // 🌟 LA SOLUCIÓN: Replicamos el agrupamiento por características del diseño
        // y sumamos las visitas individuales de cada talla con SUM(IFNULL(...))
        String sql = "SELECT " +
                     "  MIN(p.Prenda_id) AS Prenda_id, " + 
                     "  p.Prenda_nombre, " +
                     "  MIN(p.Prenda_valor) AS Prenda_valor, " + 
                     "  GROUP_CONCAT(DISTINCT p.Prenda_talla ORDER BY p.Prenda_talla SEPARATOR ', ') AS Prenda_talla, " + 
                     "  MAX(i.Imagenes_link) AS Imagen_Link, " + 
                     "  SUM(p.Prenda_stock) AS Prenda_stock, " + 
                     "  p.Prenda_estado, " +
                     "  SUM(IFNULL(pop.Populares_visitas, 0)) AS visitas_totales " + 
                     "FROM Prendas p " +
                     "LEFT JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "WHERE p.Prenda_estado = 'activa' AND p.Prenda_stock > 0 " + 
                     "GROUP BY p.Prenda_nombre, p.Prenda_tipo, p.Prenda_descripcion, p.Prenda_estado " +
                     "ORDER BY visitas_totales DESC " +
                     "LIMIT ?";
        
        //Define  la tabla principal de prendas apartir del from prendsa

        try (Connection conn = ClaseConexion.getConexion()) {
            if (conn == null) return listaPopulares;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limite);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Prendas prenda = new Prendas();
                        // Mapeo seguro con los alias calculados en la consulta unificada
                        prenda.setId(rs.getInt("Prenda_id"));
                        prenda.setNombre(rs.getString("Prenda_nombre"));
                        prenda.setValor(rs.getDouble("Prenda_valor"));
                        prenda.setTalla(rs.getString("Prenda_talla")); // Traerá por ej: "S, M, L"
                        prenda.setImagen(rs.getString("Imagen_Link")); 
                        prenda.setStock(rs.getInt("Prenda_stock")); // Sumatoria total de existencias
                        prenda.setEstado(rs.getString("Prenda_estado"));
                        listaPopulares.add(prenda);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //Retorna la lista de elementos al servetlPopulares
        return listaPopulares;
    }
}
