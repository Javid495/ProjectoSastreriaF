package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Dtos.Dtos.*; 

//DAO que solicita y genera el historial de pagos del local

public class HistorialPagosDAO {
    
    // Método principal que unifica el reporte administrativo
    public ReporteCajaDTO obtenerReporteAdministrativo(Connection conn) throws SQLException {
        MetricasDTO metricas = obtenerMetricas(conn);
        List<PagoDTO> listaPagos = obtenerListaPagos(conn);
        return new ReporteCajaDTO(metricas, listaPagos);
        
        //Este retorna al servelt Historial pagos
    }

    // ?se Calcula directamente desde el total y la fecha de la tabla Pedidos
    private MetricasDTO obtenerMetricas(Connection conn) throws SQLException {
        String sql = "SELECT " +
                //Este aprtado realiza las metricas de forma Diaria, Semanal o mensual teniendo como referencia la fehca en al cual se realiza el pedido
                //Caso de pedidos que sean diarios
                "IFNULL(SUM(CASE WHEN Pedido_FechaInicio = CURDATE() THEN Pedido_TotalCompra ELSE 0 END), 0) AS diario, " +
                //Caso de que los pedidos sean semanales
                "IFNULL(SUM(CASE WHEN YEARWEEK(Pedido_FechaInicio, 1) = YEARWEEK(CURDATE(), 1) THEN Pedido_TotalCompra ELSE 0 END), 0) AS semanal, " +
                //Caso de que los pedidos sean mensuales
                "IFNULL(SUM(CASE WHEN MONTH(Pedido_FechaInicio) = MONTH(CURDATE()) AND YEAR(Pedido_FechaInicio) = YEAR(CURDATE()) THEN Pedido_TotalCompra ELSE 0 END), 0) AS mensual " +
                //De la tabla de pedidos
                "FROM Pedidos";
        
        //Prepara o crea un canal de comunicacion para ejecutar y traer la consulta
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                //En este caso la consulta retornaria 3 valores, el total diario, semanal y mensual
                return new MetricasDTO(rs.getDouble("diario"), rs.getDouble("semanal"), rs.getDouble("mensual"));
            }
        }
        
        //En dato caso las metricas no retornan algun resultado por defecto se pondra 0 al resultado
        return new MetricasDTO(0, 0, 0);
    }

    // 📋 LISTADO: Consulta basada en Pedidos con LEFT JOIN para asegurar que se muestren aunque falten datos de usuario
    private List<PagoDTO> obtenerListaPagos(Connection conn) throws SQLException {
    List<PagoDTO> pagos = new ArrayList<>();
    
    // Consulta avanzada para rescatar al usuario desde cualquiera de los dos flujos
    String sql = "SELECT " +
        // Identificador único del pago/pedido
        "p.Pedido_id AS idPago, " +
        
        // Revisa el nombre en el Camino A; si es NULL, 
        // toma el del Camino B. 
            
        // MAX() asegura que la consulta sea compatible con las reglas estrictas de MySQL.
        "IFNULL(MAX(COALESCE(r_cat.Registro_Usuario, r_med.Registro_Usuario)), 'Cliente Temporal') AS usuario, " +
        
        // Datos financieros y de control del pedido
        "p.Pedido_TotalCompra AS total, " +
        "p.Pedido_MetodoPago AS metodoPago, " +
        "p.Pedido_FechaInicio AS fecha, " +
        "p.Pedido_TipoPedido AS tipoCompra " +
        
        // --- PUNTO DE PARTIDA ---
        "FROM Pedidos p " +
        
        // --- EL PUENTE UNIVERSAL ---
        // Conectamos el Pedido con su tabla de detalles. Esta tabla es la llave
        // que nos dice si el pedido viene de un carrito o de una cotización a medida.
        "LEFT JOIN DetallesPedidos dp ON p.Pedido_id = dp.Pedido_id " +
        

        // Si el pedido tiene un 'DetallesCarrito_id', viajamos por aquí:
        "LEFT JOIN DetallesCarrito dc ON dp.DetallesCarrito_id = dc.DetallesCarrito_Id " + // Detalle del carrito
        "LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id " +                          // Contenedor del carrito
        "LEFT JOIN Usuarios u_cat ON c.Usuarios_id = u_cat.Usuarios_id " +                // Usuario dueño del carrito
        "LEFT JOIN Registro r_cat ON u_cat.Registro_id = r_cat.Registro_id " +            // Credenciales y nombre del usuario
        
        // Si el pedido tiene un 'CotizacionPedido_id', el sistema ignora el Camino A y viaja por aquí:
        "LEFT JOIN CotizacionPedido cp ON dp.CotizacionPedido_id = cp.CotizacionPedido_id " +                // La cotización aprobada
        "LEFT JOIN DetallesPedidosMedida dpm ON cp.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " + // Los datos del diseño a medida
        "LEFT JOIN Usuarios u_med ON dpm.Usuario_id = u_med.Usuarios_id " +                                  // Usuario que solicitó el diseño
        "LEFT JOIN Registro r_med ON u_med.Registro_id = r_med.Registro_id " +                                // Credenciales y nombre del usuario
        

        // Agrupamos por los datos del pedido. Esto evita que si un cliente compró 3 prendas 
        // en el mismo carrito, el pago aparezca repetido 3 veces en la tabla del administrador.
        "GROUP BY p.Pedido_id, p.Pedido_TotalCompra, p.Pedido_MetodoPago, p.Pedido_FechaInicio, p.Pedido_TipoPedido " +
        
        // Ordenamos para que los pagos más recientes aparezcan de primeros en la lista de caja
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