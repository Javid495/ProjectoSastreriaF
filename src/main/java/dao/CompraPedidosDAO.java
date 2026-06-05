package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class CompraPedidosDAO {

    // ==========================================================================
    // 🛒 FLUJO 1: REGISTRAR COMPRA DESDE EL CARRITO DE CATÁLOGO (Intacto)
    // ==========================================================================
    public boolean registrarFlujoCompletoCompra(int idUsuario, String direccion, String telefono, 
                                                String metodoPago, String tipoPedido, List<int[]> listaProductos) {
        Connection con = null; 
        PreparedStatement psCarrito = null; 
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;  
        PreparedStatement psPago = null;
        PreparedStatement psPedido = null;
        ResultSet rs = null;

        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); 

            String sqlCarrito = "INSERT INTO Carrito (Usuarios_id, Carrito_fecha) VALUES (?, CURDATE())";
            psCarrito = con.prepareStatement(sqlCarrito, Statement.RETURN_GENERATED_KEYS);
            psCarrito.setInt(1, idUsuario);
            psCarrito.executeUpdate();

            rs = psCarrito.getGeneratedKeys();
            int idCarritoGenerado = 0;
            if (rs.next()) {
                idCarritoGenerado = rs.getInt(1); 
            }

            String sqlDetalle = "INSERT INTO DetallesCarrito (Prendas_id, Carrito_id, Detalles_stock, Detalles_total) VALUES (?, ?, ?, ?)";
            psDetalle = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);

            String sqlStock = "UPDATE Prendas SET Prenda_stock = Prenda_stock - ? WHERE Prenda_id = ?";
            psStock = con.prepareStatement(sqlStock);

            int idDetalleGenerado = 0;
            
            for (int[] prod : listaProductos) {
                int idPrenda = prod[0];
                int cantidad = prod[1];      
                double totalLinea = prod[2];  

                psDetalle.setInt(1, idPrenda);
                psDetalle.setInt(2, idCarritoGenerado);
                psDetalle.setInt(3, cantidad);
                psDetalle.setDouble(4, totalLinea);
                psDetalle.executeUpdate();

                ResultSet rsDet = psDetalle.getGeneratedKeys();
                if (rsDet.next()) {
                    idDetalleGenerado = rsDet.getInt(1);
                }

                psStock.setInt(1, cantidad);  
                psStock.setInt(2, idPrenda);  
                psStock.executeUpdate();
            }

            String sqlPago = "INSERT INTO ConfirmarPago (CotizacionPedido_Id, DetallesCarrito_id, "
                           + "ConfirmarPago_TipoPedido, ConfirmarPago_MetodoP, ConfirmarPago_Fecha, ConfirmarTelefono) "
                           + "VALUES (null, ?, ?, ?, CURDATE(), ?)";
            
            psPago = con.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS);
            psPago.setInt(1, idDetalleGenerado); 
            psPago.setString(2, tipoPedido);     
            psPago.setString(3, metodoPago);     
            psPago.setString(4, telefono);       
            psPago.executeUpdate();

            int idConfirmarPagoGenerado = 0;
            ResultSet rsPago = psPago.getGeneratedKeys();
            if (rsPago.next()) {
                idConfirmarPagoGenerado = rsPago.getInt(1);
            }

            String sqlPedido = "INSERT INTO Pedidos (ConfirmarPago_id, Pedido_FechaInicio, Pedido_Estado, "
                             + "Pedido_Direcccion, Pedido_TCompra) VALUES (?, CURDATE(), 'Pendiente', ?, ?)";
            
            psPedido = con.prepareStatement(sqlPedido);
            psPedido.setInt(1, idConfirmarPagoGenerado);
            psPedido.setString(2, direccion);    
            psPedido.setString(3, tipoPedido);   
            psPedido.executeUpdate();

            con.commit();
            System.out.println("🚀 Pedido registrado e inventario actualizado con éxito para el Carrito #" + idCarritoGenerado);
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error crítico en la transacción (Rollback activado): " + e.getMessage());
            if (con != null) {
                try { 
                    con.rollback(); 
                    System.out.println("🔄 Base de datos restaurada al estado original.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (psCarrito != null) psCarrito.close();
                if (psDetalle != null) psDetalle.close();
                if (psStock != null) psStock.close(); 
                if (psPago != null) psPago.close();
                if (psPedido != null) psPedido.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar componentes: " + e.getMessage());
            }
        }
    }
    
    // ==========================================================================
    // 🧵 FLUJO 2: REGISTRAR COMPRA DESDE PEDIDOS A MEDIDA (Cotizaciones)
    // ==========================================================================
    public boolean registrarFlujoCompletoCompraAMedida(int idUsuario, String direccion, String telefono, 
                                                       String metodoPago, String tipoPedido, int idCotizacion) {
        Connection con = null;
        PreparedStatement psPago = null;
        PreparedStatement psPedido = null;
        ResultSet rs = null;
        boolean todoOk = false;

        try {
            // 🛠️ CORRECCIÓN 1: Enlazado con tu gestor de conexiones real
            con = ClaseConexion.getConexion(); 
            con.setAutoCommit(false); 

            // 1. Insertar en ConfirmarPago (CotizacionPedido_id recibe el ID de la oferta del sastre)
            String sqlPago = "INSERT INTO ConfirmarPago (CotizacionPedido_Id, DetallesCarrito_id, "
                           + "ConfirmarPago_TipoPedido, ConfirmarPago_MetodoP, ConfirmarPago_Fecha, ConfirmarTelefono) "
                           + "VALUES (?, NULL, ?, ?, CURDATE(), ?)";
            
            psPago = con.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS);
            psPago.setInt(1, idCotizacion);
            psPago.setString(2, tipoPedido);
            psPago.setString(3, metodoPago);
            psPago.setString(4, telefono);
            psPago.executeUpdate();

            rs = psPago.getGeneratedKeys();
            int idConfirmarPago = 0;
            if (rs.next()) {
                idConfirmarPago = rs.getInt(1);
            }

            // 2. Insertar el registro final en la cola de producción (Pedidos)
            String sqlPedido = "INSERT INTO Pedidos (ConfirmarPago_Id, Pedido_FechaInicio, Pedido_Estado, "
                             + "Pedido_Direcccion, Pedido_TCompra) VALUES (?, CURDATE(), 'Pendiente', ?, ?)";
            
            psPedido = con.prepareStatement(sqlPedido);
            psPedido.setInt(1, idConfirmarPago);
            psPedido.setString(2, direccion);
            psPedido.setString(3, tipoPedido);
            psPedido.executeUpdate();

            con.commit(); 
            System.out.println("🚀 Éxito transaccional: Cotización #" + idCotizacion + " convertida en Pedido en producción.");
            todoOk = true;

        } catch (Exception e) {
            System.out.println("❌ Error en transacción de Pedido a Medida (Rollback activado): " + e.getMessage());
            if (con != null) {
                try { 
                    con.rollback(); 
                    System.out.println("🔄 Cambios revertidos en la base de datos.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            // 🛠️ CORRECCIÓN 2: Cierre seguro de memoria y descriptores de archivos
            try {
                if (rs != null) rs.close();
                if (psPago != null) psPago.close();
                if (psPedido != null) psPedido.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar componentes en Flujo a Medida: " + e.getMessage());
            }
        }
        return todoOk;
    }
}