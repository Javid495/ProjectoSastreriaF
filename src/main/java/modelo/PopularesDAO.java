package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import getsSets.Prendas;

public class PopularesDAO {

    /**
     * Registra o incrementa la visita de una prenda en caliente.
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
     * Corregido para evitar el error ONLY_FULL_GROUP_BY de MySQL.
     */
    public List<Prendas> obtenerTopPopulares(int limite) {
        List<Prendas> listaPopulares = new ArrayList<>();
        
        // 🔥 LA SOLUCIÓN: Agregamos MAX(...) en la cláusula ORDER BY para cumplir con el estándar SQL estricto
        String sql = "SELECT p.*, MIN(i.Imagenes_link) AS Imagen_Link " +
                     "FROM Prendas p " +
                     "JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "WHERE p.Prenda_estado = 'activa' " + 
                     "GROUP BY p.Prenda_id " +
                     "ORDER BY MAX(CAST(pop.Populares_visitas AS UNSIGNED)) DESC " +
                     "LIMIT ?";

        // Estructura segura: validamos primero la conexión de la BD
        try (Connection conn = ClaseConexion.getConexion()) {
            if (conn == null) return listaPopulares;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limite);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Prendas prenda = new Prendas();
                        prenda.setId(rs.getInt("Prenda_id"));
                        prenda.setNombre(rs.getString("Prenda_nombre"));
                        prenda.setValor(rs.getDouble("Prenda_valor"));
                        prenda.setTalla(rs.getString("Prenda_talla"));
                        prenda.setImagen(rs.getString("Imagen_Link")); 
                        prenda.setStock(rs.getInt("Prenda_stock"));
                        prenda.setEstado(rs.getString("Prenda_estado"));
                        listaPopulares.add(prenda);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaPopulares;
    }
}
