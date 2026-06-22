package modelo;

import getsSets.HistorialUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialUsuarioDAO {

    /**
     * MÈTODO 1: Registrar una acción (Escritura)
     * Se invoca desde otros DAOs o Servlets tras una transacción exitosa.
     */
    public boolean registrarAccion(int usuarioId, String accion, String tabla, int registroId, String descripcion) {
        String sql = "INSERT INTO HistorialUsuario (Usuarios_id, HistorialAccion, HistoriaTablaAfectada, "
                   + "HistorialRegistroAfectado_id, HistorialDescripcion) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = ClaseConexion.getConexion(); // Reemplaza por tu método de conexión
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, tabla);
            ps.setInt(4, registroId);
            ps.setString(5, descripcion);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al escribir en HistorialUsuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * MÈTODO 2: Obtener bitácora de un usuario (Lectura para el Admin)
     * Separa el TIMESTAMP de MySQL en fecha y hora independientes para tu JSON.
     */
    public List<HistorialUsuario> obtenerHistorialPorUsuario(int usuarioId) {
        List<HistorialUsuario> lista = new ArrayList<>();
        String sql = "SELECT HistorialUsuario_id, HistorialAccion, HistorialDescripcion, "
                   + "DATE(HistorialFechaHora) AS fecha_limpia, "
                   + "TIME(HistorialFechaHora) AS hora_limpia "
                   + "FROM HistorialUsuario "
                   + "WHERE Usuarios_id = ? "
                   + "ORDER BY HistorialFechaHora DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialUsuario log = new HistorialUsuario();
                    log.setId(rs.getInt("HistorialUsuario_id"));
                    log.setAccion(rs.getString("HistorialAccion"));
                    log.setDescripcion(rs.getString("HistorialDescripcion"));
                    log.setFecha(rs.getString("fecha_limpia"));
                    log.setHora(rs.getString("hora_limpia"));
                    
                    lista.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al leer HistorialUsuario: " + e.getMessage());
        }
        return lista;
    }
}
