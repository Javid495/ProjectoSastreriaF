package dao; // 🚨 Ajusta este paquete según la estructura de tu proyecto

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// Ajusta el import de tu clase de conexión si se llama diferente o está en otro paquete

public class EliminarPrendasDAO {

    // ============================================================================
    // 1. ELIMINACIÓN INDIVIDUAL (Para el botón de cada tarjeta)
    // ============================================================================
    public boolean eliminarPrendaCompleta(int idPrenda) {
        String sqlFotos = "DELETE FROM imagenes WHERE Prenda_id = ?;";
        String sqlPrenda = "DELETE FROM Prendas WHERE Prenda_id = ?;";

        Connection con = null;
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // Transacción activa para asegurar consistencia

            // Paso A: Limpiamos las fotos amarradas a la prenda
            try (PreparedStatement psFotos = con.prepareStatement(sqlFotos)) {
                psFotos.setInt(1, idPrenda);
                psFotos.executeUpdate();
            }

            // Paso B: Borramos la prenda base
            try (PreparedStatement psPrenda = con.prepareStatement(sqlPrenda)) {
                psPrenda.setInt(1, idPrenda);
                int filasAfectadas = psPrenda.executeUpdate();

                if (filasAfectadas > 0) {
                    con.commit(); // Consolidamos la eliminación dual
                    System.out.println("DAO: Prenda ID " + idPrenda + " y sus imágenes eliminadas con éxito.");
                    return true;
                }
            }

            if (con != null) con.rollback();
            return false;

        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.out.println("DAO Error en eliminación individual: " + e.getMessage());
            return false;
        } finally {
            if (con != null) { 
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); } 
            }
        }
    }

    // ============================================================================
    // 2. ELIMINACIÓN GRUPAL / MASIVA (Para borrar mediante Checkboxes)
    // ============================================================================
    public boolean eliminarPrendasEnMasa(List<Integer> listaIds) {
        if (listaIds == null || listaIds.isEmpty()) {
            return false;
        }

        // Construimos los signos de interrogación necesarios según el tamaño de la lista.
        // Ejemplo si vienen 3 IDs: (?, ?, ?)
        StringBuilder signosInterrogacion = new StringBuilder();
        for (int i = 0; i < listaIds.size(); i++) {
            signosInterrogacion.append("?");
            if (i < listaIds.size() - 1) {
                signosInterrogacion.append(", ");
            }
        }

        // Consultas dinámicas optimizadas con la cláusula IN
        String sqlFotosMasivo = "DELETE FROM imagenes WHERE Prenda_id IN (" + signosInterrogacion + ");";
        String sqlPrendasMasivo = "DELETE FROM Prendas WHERE Prenda_id IN (" + signosInterrogacion + ");";

        Connection con = null;
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // Transacción global: se borran todas las prendas del lote o ninguna

            // Paso A: Borrar en masa todas las imágenes de las prendas seleccionadas
            try (PreparedStatement psFotos = con.prepareStatement(sqlFotosMasivo)) {
                for (int i = 0; i < listaIds.size(); i++) {
                    psFotos.setInt(i + 1, listaIds.get(i));
                }
                psFotos.executeUpdate();
            }

            // Paso B: Borrar en masa todas las prendas seleccionadas
            try (PreparedStatement psPrendas = con.prepareStatement(sqlPrendasMasivo)) {
                for (int i = 0; i < listaIds.size(); i++) {
                    psPrendas.setInt(i + 1, listaIds.get(i));
                }
                int filasAfectadas = psPrendas.executeUpdate();

                // Si se borró al menos una prenda del lote, consideramos la operación exitosa
                if (filasAfectadas > 0) {
                    con.commit();
                    System.out.println("DAO: Eliminación masiva completada. Prendas borradas: " + filasAfectadas);
                    return true;
                }
            }

            if (con != null) con.rollback();
            return false;

        } 
        
        catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.out.println("DAO Error en eliminación masiva: " + e.getMessage());
            return false;
        } 
        
        finally {
            if (con != null) { 
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); } 
            }
        }
    }
}