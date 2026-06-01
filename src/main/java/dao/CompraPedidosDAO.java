package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;


public class CompraPedidosDAO {

    public boolean registrarFlujoCompletoCompra(int idUsuario, String direccion, String telefono, 
                                                String metodoPago, String tipoPedido, List<int[]> listaProductos) {
        Connection con = null; //Se establece la conexion a la base de datos
        
        //Se toman los datos del proceso
        PreparedStatement psCarrito = null; 
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;  
        PreparedStatement psPago = null;
        PreparedStatement psPedido = null;
        ResultSet rs = null;

        try {
            con = ClaseConexion.getConexion();
            
            
            con.setAutoCommit(false); 

            // Se hace la insercion en la tabla compra
            String sqlCarrito = "INSERT INTO Carrito (Usuarios_id, Carrito_fecha) VALUES (?, CURDATE())";
            psCarrito = con.prepareStatement(sqlCarrito, Statement.RETURN_GENERATED_KEYS);
            psCarrito.setInt(1, idUsuario);
            psCarrito.executeUpdate();

            rs = psCarrito.getGeneratedKeys();
            int idCarritoGenerado = 0;
            if (rs.next()) {
                idCarritoGenerado = rs.getInt(1); 
            }

            //Se preparara laa insercion en la tabla detallesCarrito
            String sqlDetalle = "INSERT INTO DetallesCarrito (Prendas_id, Carrito_id, Detalles_total) VALUES (?, ?, ?)";
            psDetalle = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);

            // Query para restar las unidades compradas del stock actual en la tabla Prendas
            String sqlStock = "UPDATE Prendas SET Prenda_stock = Prenda_stock - ? WHERE Prenda_id = ?";
            psStock = con.prepareStatement(sqlStock);

            int idDetalleGenerado = 0;

            //Se recorren los productos se guardan lo detalles y se descuenta el stock
            for (int[] prod : listaProductos) {
                int idPrenda = prod[0];
                int cantidad = prod[1];      // 👈 ¡Aquí usamos la cantidad!
                double totalLinea = prod[2];  // (Precio * Cantidad)

                // A) Insertar el detalle financiero de la prenda
                psDetalle.setInt(1, idPrenda);
                psDetalle.setInt(2, idCarritoGenerado);
                psDetalle.setDouble(3, totalLinea);
                psDetalle.executeUpdate();

                // Recuperamos el ID del detalle para la relación posterior
                ResultSet rsDet = psDetalle.getGeneratedKeys();
                if (rsDet.next()) {
                    idDetalleGenerado = rsDet.getInt(1);
                }

                // B) Descontar el Stock físicamente en la base de datos
                psStock.setInt(1, cantidad);  // Resta N unidades
                psStock.setInt(2, idPrenda);  // A la prenda específica
                psStock.executeUpdate();
            }

            //Se realiza la insercion en la tabla de confirmar pago
            String sqlPago = "INSERT INTO ConfirmarPago (CotizacionPedido_id, DetallesCarrito_id, "
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

            //Se inserta el nuevo pedido de tipo compraCatallogo en la tabla de pedidos
            String sqlPedido = "INSERT INTO Pedidos (ConfirmarPago_id, Pedido_FechaInicio, Pedido_Estado, "
                             + "Pedido_Direcccion, Pedido_TCompra) VALUES (?, CURDATE(), 'Pendiente', ?, ?)";
            
            psPedido = con.prepareStatement(sqlPedido);
            psPedido.setInt(1, idConfirmarPagoGenerado);
            psPedido.setString(2, direccion);    
            psPedido.setString(3, tipoPedido);   
            psPedido.executeUpdate();

            // ¡TODO ÉXITO! Guardamos cambios en lote de forma segura
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
        } 
        
        finally {
            // Cierre ordenado de todos los recursos
            try {
                if (rs != null) rs.close();
                if (psCarrito != null) psCarrito.close();
                if (psDetalle != null) psDetalle.close();
                if (psStock != null) psStock.close(); // 👈 Cerramos el nuevo flujo
                if (psPago != null) psPago.close();
                if (psPedido != null) psPedido.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("Error al cerrar componentes: " + e.getMessage());
            }
        }
    }
}