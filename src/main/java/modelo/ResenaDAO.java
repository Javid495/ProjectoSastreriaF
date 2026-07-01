package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Dtos.Resenas;

public class ResenaDAO {

    // MÈTODO 1: Guardar la reseña escrita por el usuario logueado
    public boolean agregarResena(Resenas r) {
        // Respetamos el nombre de la columna con 'ñ' tal como está en tu script de BD
        String sql = "INSERT INTO Resenas (Usuarios_id, Prenda_id, Resena_descripcion, Reseña_Imagen) VALUES (?, ?, ?, ?)";
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, r.getUsuarioId());
            ps.setInt(2, r.getPrendaId());
            ps.setString(3, r.getDescripcion());
            ps.setString(4, r.getImagenResena()); // Ruta por defecto o cargada
            
            return ps.executeUpdate() > 0;
        } 
        
        catch (SQLException e) {
            System.err.println("Error al insertar reseña: " + e.getMessage());
            return false;
        }
    }

    // MÈTODO 2: Listar reseñas asociadas a una prenda específica
    public List<Resenas> obtenerResenasPorPrenda(int prendaId) {
        List<Resenas> lista = new ArrayList<>();
        String sql = "SELECT re.Resena_id, re.Resena_descripcion, re.Reseña_Imagen, "
                   + "r.Registro_Usuario, u.Usuario_imagen "
                   + "FROM Resenas re "
                   + "INNER JOIN Usuarios u ON re.Usuarios_id = u.Usuarios_id "
                   + "INNER JOIN Registro r ON u.Registro_id = r.Registro_id "
                   + "WHERE re.Prenda_id = ? "
                   + "ORDER BY re.Resena_id DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, prendaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Resenas r = new Resenas();
                    r.setId(rs.getInt("Resena_id"));
                    r.setDescripcion(rs.getString("Resena_descripcion"));
                    r.setImagenResena(rs.getString("Reseña_Imagen"));
                    r.setNombreUsuario(rs.getString("Registro_Usuario"));
                    r.setAvatarUsuario(rs.getString("Usuario_imagen"));
                    lista.add(r);
                }
            }
        } 
        
        catch (SQLException e) {
            System.err.println("Error al recuperar reseñas: " + e.getMessage());
        }
        return lista;
    }
}
