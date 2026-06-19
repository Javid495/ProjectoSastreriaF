package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import getsSets.Prendas;
import modelo.ClaseConexion;

public class PopularesDAO {

    /**
     * Registra o incrementa la visita de una prenda en caliente.
     */
    
    //Se crea el modelo de prenda trayendonos el id de prenda para añadir la visita a esta prenda
    public void registrarVisita(int idPrenda) {
        
        // Pregunta a la base de datos si existe registro de la prenda en populares
        String sqlCheck = "SELECT COUNT(*) FROM Populares WHERE Prenda_id = ?";
        // Actualiza el contador de la prenda en especifico
        String sqlUpdate = "UPDATE Populares SET Populares_visitas = CAST(Populares_visitas AS UNSIGNED) + 1 WHERE Prenda_id = ?";
        
        //En caso de que no haya registro de vista de la prenda
        String sqlInsert = "INSERT INTO Populares (Prenda_id, Populares_visitas) VALUES (?, '1')";

        try (Connection conn = ClaseConexion.getConexion()) {
            if (conn == null) return;

            // Verificar si la prenda ya tiene un registro en populares
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idPrenda);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Si ya existe, sumamos 1
                        try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                            psUpdate.setInt(1, idPrenda);
                            psUpdate.executeUpdate();
                        }
                    } else {
                        // Si es la primera vez que la ven, la insertamos con valor 1
                        try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                            psInsert.setInt(1, idPrenda);
                            psInsert.executeUpdate();
                        }
                    }
                }
            }
        } 
        // En caso de que suceda algun error en la inserccion
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Trae la lista de las prendas más vistas ordenadas de mayor a menor.
     */
    public List<Prendas> obtenerTopPopulares(int limite) {
        List<Prendas> listaPopulares = new ArrayList<>();
        // Query que une Prendas con Populares y se trae la primera imagen de la prenda
        String sql = "SELECT p.*, MIN(i.Imagenes_link) AS Imagen_Link " +
                     "FROM Prendas p " +
                     "JOIN Populares pop ON p.Prenda_id = pop.Prenda_id " +
                     "LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id " +
                     "WHERE p.Prenda_estado = 'Activo' " + // O el estado que manejes
                     "GROUP BY p.Prenda_id " +
                     "ORDER BY CAST(pop.Populares_visitas AS UNSIGNED) DESC " +
                     "LIMIT ?";

        try (Connection conn = ClaseConexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prendas prenda = new Prendas();
                    prenda.setId(rs.getInt("Prenda_id"));
                    prenda.setNombre(rs.getString("Prenda_nombre"));
                    prenda.setValor(rs.getDouble("Prenda_valor"));
                    prenda.setTalla(rs.getString("Prenda_talla"));
                    // Aquí le pasas la ruta de la imagen que recuperamos en el JOIN
                    prenda.setImagen(rs.getString("Imagen_Link")); 
                    
                    listaPopulares.add(prenda);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listaPopulares;
    }
}
