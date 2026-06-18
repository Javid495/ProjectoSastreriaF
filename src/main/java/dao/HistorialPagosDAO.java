package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Dtos.*; 

public class HistorialPagosDAO {
    
    // Método principal que unifica el reporte administrativo
    public ReporteCajaDTO obtenerReporteAdministrativo(Connection conn) throws SQLException {
        MetricasDTO metricas = obtenerMetricas(conn);
        List<PagoDTO> listaPagos = obtenerListaPagos(conn);
        return new ReporteCajaDTO(metricas, listaPagos);
    }

    // 📊 MÉTRICAS: Calculadas directamente desde el total y la fecha de la tabla Pedidos
    private MetricasDTO obtenerMetricas(Connection conn) throws SQLException {
        String sql = "SELECT " +
                "IFNULL(SUM(CASE WHEN Pedido_FechaInicio = CURDATE() THEN Pedido_TotalCompra ELSE 0 END), 0) AS diario, " +
                "IFNULL(SUM(CASE WHEN YEARWEEK(Pedido_FechaInicio, 1) = YEARWEEK(CURDATE(), 1) THEN Pedido_TotalCompra ELSE 0 END), 0) AS semanal, " +
                "IFNULL(SUM(CASE WHEN MONTH(Pedido_FechaInicio) = MONTH(CURDATE()) AND YEAR(Pedido_FechaInicio) = YEAR(CURDATE()) THEN Pedido_TotalCompra ELSE 0 END), 0) AS mensual " +
                "FROM Pedidos";
        
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new MetricasDTO(rs.getDouble("diario"), rs.getDouble("semanal"), rs.getDouble("mensual"));
            }
        }
        return new MetricasDTO(0, 0, 0);
    }

    // 📋 LISTADO: Consulta basada en Pedidos con LEFT JOIN para asegurar que se muestren aunque falten datos de usuario
    private List<PagoDTO> obtenerListaPagos(Connection conn) throws SQLException {
        List<PagoDTO> pagos = new ArrayList<>();
        String sql = "SELECT " +
                "p.Pedido_id AS idPago, " +
                "IFNULL(r.Registro_Usuario, 'Cliente Temporal') AS usuario, " +
                "p.Pedido_TotalCompra AS total, " +
                "p.Pedido_MetodoPago AS metodoPago, " +
                "p.Pedido_FechaInicio AS fecha, " +
                "p.Pedido_TipoPedido AS tipoCompra " +
                "FROM Pedidos p " +
                "LEFT JOIN Usuarios u ON p.Usuario_id = u.Usuarios_id " +
                "LEFT JOIN Registro r ON u.Registro_id = r.Registro_id " +
                "ORDER BY p.Pedido_FechaInicio DESC";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pagos.add(new PagoDTO(
                    rs.getInt("idPago"),
                    rs.getString("usuario"),
                    rs.getDouble("total"),
                    rs.getString("metodoPago"),
                    rs.getString("fecha"),
                    rs.getString("tipoCompra")
                ));
            }
        }
        return pagos;
    }
}