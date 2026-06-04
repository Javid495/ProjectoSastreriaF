package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.Dtos.*; // Al importar con .* traemos todas las clases estáticas internas (PagoDTO, MetricasDTO, etc.)

public class HistorialPagosDAO {
    
    // Método para obtener todo el consolidado administrativo
    public ReporteCajaDTO obtenerReporteAdministrativo(Connection conn) throws SQLException {
        MetricasDTO metricas = obtenerMetricas(conn);
        List<PagoDTO> listaPagos = obtenerListaPagos(conn);
        return new ReporteCajaDTO(metricas, listaPagos);
    }

    private MetricasDTO obtenerMetricas(Connection conn) throws SQLException {
        String sql = "SELECT " +
                "IFNULL(SUM(CASE WHEN hp.Historial_Fecha = CURDATE() THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS diario, " +
                "IFNULL(SUM(CASE WHEN YEARWEEK(hp.Historial_Fecha, 1) = YEARWEEK(CURDATE(), 1) THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS semanal, " +
                "IFNULL(SUM(CASE WHEN MONTH(hp.Historial_Fecha) = MONTH(CURDATE()) AND YEAR(hp.Historial_Fecha) = YEAR(CURDATE()) THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS mensual " +
                "FROM HistorialPagos hp " +
                "JOIN ConfirmarPago cf ON hp.ConfirmarPago_id = cf.ConfirmarPago_id " +
                "LEFT JOIN DetallesCarrito dc ON cf.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                "LEFT JOIN CotizacionPedido cp ON cf.CotizacionPedido_id = cp.CotizacionPedido_Id";
        
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new MetricasDTO(rs.getDouble("diario"), rs.getDouble("semanal"), rs.getDouble("mensual"));
            }
        }
        return new MetricasDTO(0, 0, 0);
    }

    private List<PagoDTO> obtenerListaPagos(Connection conn) throws SQLException {
        List<PagoDTO> pagos = new ArrayList<>();
        String sql = "SELECT hp.HistorialPagos_id AS idPago, r.Registro_Usuario AS usuario, " +
                "COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) AS total, cf.ConfirmarPago_MetodoP AS metodoPago, " +
                "hp.Historial_Fecha AS fecha, cf.ConfirmarPago_TipoPedido AS tipoCompra " +
                "FROM HistorialPagos hp " +
                "JOIN ConfirmarPago cf ON hp.ConfirmarPago_id = cf.ConfirmarPago_id " +
                "LEFT JOIN DetallesCarrito dc ON cf.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                "LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id " +
                "LEFT JOIN CotizacionPedido cp ON cf.CotizacionPedido_id = cp.CotizacionPedido_Id " +
                "LEFT JOIN DetallesPedidosMedida dpm ON cp.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                "JOIN Usuarios u ON u.Usuarios_id = COALESCE(c.Usuarios_id, dpm.Usuario_id) " +
                "JOIN Registro r ON u.Registro_id = r.Registro_id " +
                "ORDER BY hp.Historial_Fecha DESC";

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
