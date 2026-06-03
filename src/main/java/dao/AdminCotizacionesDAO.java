package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminCotizacionesDAO {

    // 1. Cuenta las solicitudes donde Detalles_Cotizacion sigue siendo NULL
    public int contarPendientesPorCotizar() {
        String sql = "SELECT COUNT(*) FROM DetallesPedidosMedida WHERE Detalles_Cotizacion IS NULL";
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Error contando cotizaciones: " + e.getMessage());
        }
        return 0;
    }

    // 2. Trae el listado cruzando tablas para saber qué usuario lo solicitó
    public List<String[]> listarPedidosPorCotizar() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT dpm.Detalles_PedidoMedida_id, r.Registro_Email, dpm.Detalles_TPrenda, " +
                     "dpm.Detalles_Tela, dpm.Detalles_medidas, dpm.Detalles_Descripcion, dpm.Detalles_ImagenReferencia " +
                     "FROM DetallesPedidosMedida dpm " +
                     "JOIN Usuarios u ON dpm.Usuario_id = u.Usuarios_id " +
                     "JOIN Registro r ON u.Registro_id = r.Registro_id " +
                     "WHERE dpm.Detalles_Cotizacion IS NULL " +
                     "ORDER BY dpm.Detalles_PedidoMedida_id DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String[] fila = new String[7];
                fila[0] = String.valueOf(rs.getInt("Detalles_PedidoMedida_id"));
                fila[1] = rs.getString("Registro_Email");
                fila[2] = rs.getString("Detalles_TPrenda");
                fila[3] = rs.getString("Detalles_Tela");
                fila[4] = rs.getString("Detalles_medidas");
                fila[5] = rs.getString("Detalles_Descripcion");
                
                String img = rs.getString("Detalles_ImagenReferencia");
                fila[6] = (img != null) ? img.replace("\\", "\\\\") : "";
                
                lista.add(fila);
            }
        } catch (Exception e) {
            System.out.println("Error listando cotizaciones pendientes: " + e.getMessage());
        }
        return lista;
    }
}
