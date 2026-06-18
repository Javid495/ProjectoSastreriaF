package dao; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// Dao encargado de getionar la eliminacion de las prendas en el catalogo

public class EliminarPrendasDAO {

    // ============================================================================
    // 1. ELIMINACIÓN INDIVIDUAL LÓGICA (Para el botón de cada tarjeta)
    // ============================================================================
    public boolean eliminarPrendaCompleta(int idPrenda) {
        // En lugar de DELETE, hacemos un UPDATE al estado
        String sqlPrenda = "UPDATE Prendas SET Prenda_estado = 'eliminada' WHERE Prenda_id = ?;";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement psPrenda = con.prepareStatement(sqlPrenda)) {
            
            psPrenda.setInt(1, idPrenda);
            int filasAfectadas = psPrenda.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("DAO: Prenda ID " + idPrenda + " marcada como 'eliminada' con éxito.");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("DAO Error en eliminación lógica individual: " + e.getMessage());
            return false;
        }
    }

    // ============================================================================
    // 2. ELIMINACIÓN GRUPAL / MASIVA LÓGICA (Para borrar mediante Checkboxes)
    // ============================================================================
    public boolean eliminarPrendasEnMasa(List<Integer> listaIds) {
        if (listaIds == null || listaIds.isEmpty()) {
            return false;
        }

        // Construimos los signos de interrogación dinámicos (?, ?, ?) según el tamaño del lote
        StringBuilder signosInterrogacion = new StringBuilder();
        for (int i = 0; i < listaIds.size(); i++) {
            signosInterrogacion.append("?");
            if (i < listaIds.size() - 1) {
                signosInterrogacion.append(", ");
            }
        }

        // Modificamos el DELETE masivo por un UPDATE masivo con la cláusula IN
        String sqlPrendasMasivo = "UPDATE Prendas SET Prenda_estado = 'eliminada' WHERE Prenda_id IN (" + signosInterrogacion + ");";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement psPrendas = con.prepareStatement(sqlPrendasMasivo)) {
            
            // Inyectamos los IDs en los parámetros correspondientes
            for (int i = 0; i < listaIds.size(); i++) {
                psPrendas.setInt(i + 1, listaIds.get(i));
            }
            
            int filasAfectadas = psPrendas.executeUpdate();

            // Si se actualizaron prendas, la operación en lote fue exitosa
            if (filasAfectadas > 0) {
                System.out.println("DAO: Eliminación masiva lógica completada. Prendas marcadas como 'eliminada': " + filasAfectadas);
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("DAO Error en eliminación masiva lógica: " + e.getMessage());
            return false;
        }
    }
}