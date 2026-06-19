package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;



// Dao que maneja el proceso y el flujo de compra de los productos del catalogo y pedidos a medida
public class CompraPedidosDAO {

    
    public boolean registrarCarritoTemporal(int idUsuario, List<int[]> listaProductos) {
    Connection con = null; 
    PreparedStatement psCarrito = null; 
    PreparedStatement psDetalleCar = null;
    ResultSet rs = null;

        try {
         
            // fallido o pendiente de este mismo usuario para evitar duplicados.
            limpiarCarritoAbandono(idUsuario);

            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); // Iniciamos transacción para el carrito

            // 1. Inserción en 'Carrito'
            String sqlCarrito = "INSERT INTO Carrito (Usuarios_id, Carrito_fechaCreacion) VALUES (?, CURDATE())";
            psCarrito = con.prepareStatement(sqlCarrito, Statement.RETURN_GENERATED_KEYS);
            psCarrito.setInt(1, idUsuario);
            psCarrito.executeUpdate();

            rs = psCarrito.getGeneratedKeys();
            int idCarritoGenerado = 0;
            if (rs.next()) {
                idCarritoGenerado = rs.getInt(1); 
            }

            // 2. Inserción en 'DetallesCarrito'
            String sqlDetalleCar = "INSERT INTO DetallesCarrito (Prendas_id, Carrito_id, Detalles_cantidad) VALUES (?, ?, ?)";
            psDetalleCar = con.prepareStatement(sqlDetalleCar);

            for (int[] prod : listaProductos) {
                int idPrenda = prod[0];
                int cantidad = prod[1];      

                psDetalleCar.setInt(1, idPrenda);
                psDetalleCar.setInt(2, idCarritoGenerado);
                psDetalleCar.setInt(3, cantidad);
                psDetalleCar.addBatch();
            }
        
            psDetalleCar.executeBatch(); // Guardamos los detalles en lote

            con.commit(); // Confirmamos: ¡Datos visibles en MySQL Workbench ahora mismo!
            System.out.println("Evidencia Académica: Carrito temporal creado para Usuario ID: " + idUsuario);
            return true;

        } catch (Exception e) {
            System.out.println("Error al registrar el carrito temporal: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (psCarrito != null) psCarrito.close();
                if (psDetalleCar != null) psDetalleCar.close();
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    
    
    // ==========================================================================
    // 🛒 FLUJO 1: REGISTRAR COMPRA DESDE EL CARRITO DE CATÁLOGO
    // ==========================================================================
    public boolean confirmarPedidoDefinitivo(int idUsuario, String direccion, String telefono, 
                                         String metodoPago, String tipoPedido, List<int[]> listaProductos) {
    Connection con = null; 
    PreparedStatement psPedido = null;
    PreparedStatement psDetallePed = null;
    PreparedStatement psStock = null;  
    ResultSet rsPed = null;

    try {
        con = ClaseConexion.getConexion();
        con.setAutoCommit(false); // 🔒 Transacción unificada para el cierre

        // 1. Calcular el total de la compra desde la lista que viene del Frontend
        double totalCompra = 0;
        for (int[] prod : listaProductos) {
            totalCompra += prod[2]; // prod[2] es el subtotal de la línea
        }

        // 2. Inserción Definitiva en 'Pedidos'
        String sqlPedido = "INSERT INTO Pedidos (Usuario_id, Pedido_TipoPedido, Pedido_MetodoPago, "
                         + "Pedido_FechaInicio, Pedido_TelefonoContacto, Pedido_Direccion, Pedido_Estado, Pedido_TotalCompra) "
                         + "VALUES (?, ?, ?, CURDATE(), ?, ?, 'Pendiente', ?)";
        
        psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
        
        //Preparamos los datos para añadir a las tablas
        psPedido.setInt(1, idUsuario);
        psPedido.setString(2, tipoPedido);
        psPedido.setString(3, metodoPago);
        psPedido.setString(4, telefono);
        psPedido.setString(5, direccion);
        psPedido.setDouble(6, totalCompra);
        psPedido.executeUpdate();

        int idPedidoGenerado = 0;
        rsPed = psPedido.getGeneratedKeys();
        if (rsPed.next()) {
            idPedidoGenerado = rsPed.getInt(1);
        }

        // 3. Inserción en 'DetallesPedidos' y Actualización de Stock
        String sqlDetallePed = "INSERT INTO DetallesPedidos (Pedido_id, Prenda_id, CotizacionPedido_id, Detalles_Cantidad, Detalles_PrecioTotal) "
                             + "VALUES (?, ?, NULL, ?, ?)";
        psDetallePed = con.prepareStatement(sqlDetallePed);

        String sqlStock = "UPDATE Prendas SET Prenda_stock = Prenda_stock - ? WHERE Prenda_id = ?";
        psStock = con.prepareStatement(sqlStock);

        for (int[] prod : listaProductos) {
            int idPrenda = prod[0];
            int cantidad = prod[1];      
            double totalLinea = prod[2];  

            // Llenar detalles del pedido real
            psDetallePed.setInt(1, idPedidoGenerado);
            psDetallePed.setInt(2, idPrenda);
            psDetallePed.setInt(3, cantidad);
            psDetallePed.setDouble(4, totalLinea);
            psDetallePed.addBatch();

            // Descontar del stock real
            psStock.setInt(1, cantidad);  
            psStock.setInt(2, idPrenda);  
            psStock.addBatch();
        }
        psDetallePed.executeBatch();
        psStock.executeBatch();

        con.commit(); // Guardamos el pedido de forma segura antes de limpiar
        System.out.println("🚀 Pedido #" + idPedidoGenerado + " registrado con éxito.");

        // 4. 🗑️ LIMPIEZA INMEDIATA: Como la transacción del pedido fue exitosa,
        // ejecutamos de una vez el borrado del carrito usando el método que ya tienes.
        limpiarCarritoAbandono(idUsuario);
        
        return true;

    } catch (Exception e) {
        System.out.println("❌ Error al confirmar el pedido final (Rollback activado): " + e.getMessage());
        if (con != null) {
            try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
        }
        return false;
    } finally {
        try {
            if (rsPed != null) rsPed.close();
            if (psPedido != null) psPedido.close();
            if (psDetallePed != null) psDetallePed.close();
            if (psStock != null) psStock.close(); 
            if (con != null) con.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
    }
    
    // ==========================================================================
// 🧵 PASO B2: CONFIRMAR PEDIDO A MEDIDA DEFINITIVO (Al Confirmar Pago)
// ==========================================================================
    public boolean confirmarPedidoAMedidaDefinitivo(int idUsuario, String direccion, String telefono, 
                                                String metodoPago, String tipoPedido, int idCotizacion) {
        Connection con = null;
        PreparedStatement psGetValor = null;
        PreparedStatement psPedido = null;
        PreparedStatement psDetallePed = null;
        PreparedStatement psUpdateCotizacion = null; // 🔥 NUEVO: Para actualizar el estado de la cotización
        ResultSet rs = null;
        ResultSet rsPed = null;

        try {
            con = ClaseConexion.getConexion(); 
            con.setAutoCommit(false); // 🔒 Transacción unificada

            // 1. Consultar el valor real aprobado por el administrador
            String sqlGetValor = "SELECT Cotizacion_Valor FROM CotizacionPedido WHERE CotizacionPedido_Id = ?";
            psGetValor = con.prepareStatement(sqlGetValor);
            psGetValor.setInt(1, idCotizacion);
            rs = psGetValor.executeQuery();
        
            double totalCompra = 0;
            if (rs.next()) {
                totalCompra = rs.getDouble("Cotizacion_Valor");
            } else {
                throw new Exception("La cotización proporcionada no existe.");
            }

            // 2. Insertar directamente el registro definitivo en 'Pedidos'
            String sqlPedido = "INSERT INTO Pedidos (Usuario_id, Pedido_TipoPedido, Pedido_MetodoPago, "
                         + "Pedido_FechaInicio, Pedido_TelefonoContacto, Pedido_Direccion, Pedido_Estado, Pedido_TotalCompra) "
                         + "VALUES (?, ?, ?, CURDATE(), ?, ?, 'Pendiente', ?)";
        
            psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setInt(1, idUsuario);
            psPedido.setString(2, tipoPedido);
            psPedido.setString(3, metodoPago);
            psPedido.setString(4, telefono);
            psPedido.setString(5, direccion);
            psPedido.setDouble(6, totalCompra);
            psPedido.executeUpdate();

            int idPedidoGenerado = 0;
            rsPed = psPedido.getGeneratedKeys();
            if (rsPed.next()) {
                idPedidoGenerado = rsPed.getInt(1);
            }

            // 3. Vincular el pedido definitivo con la cotización en 'DetallesPedidos'
            String sqlDetallePed = "INSERT INTO DetallesPedidos (Pedido_id, Prenda_id, CotizacionPedido_id, Detalles_Cantidad, Detalles_PrecioTotal) "
                             + "VALUES (?, NULL, ?, 1, ?)";
            
            psDetallePed = con.prepareStatement(sqlDetallePed);
            psDetallePed.setInt(1, idPedidoGenerado);
            psDetallePed.setInt(2, idCotizacion);
            psDetallePed.setDouble(3, totalCompra);
            psDetallePed.executeUpdate();

            con.commit(); // Fin de la transacción: Todo guardado y actualizado con éxito
            System.out.println("🧵 Éxito transaccional: Cotización #" + idCotizacion + " convertida en Pedido definitivo #" + idPedidoGenerado);
            return true;

        } catch (Exception e) {
            System.out.println("❌ Error en la confirmación del Pedido a Medida (Rollback activado): " + e.getMessage());
            
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            
            return false;
        }   
        
        finally {
            try {
                if (rs != null) rs.close();
                if (rsPed != null) rsPed.close();
                if (psGetValor != null) psGetValor.close();
                if (psPedido != null) psPedido.close();
                if (psDetallePed != null) psDetallePed.close();
                if (psUpdateCotizacion != null) psUpdateCotizacion.close();
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ==========================================================================
    // 🚪 EXTRA: ELIMINACIÓN DE DATOS TEMPORALES POR ABANDONO
    // ==========================================================================
    public boolean limpiarCarritoAbandono(int idUsuario) {
        Connection con = null;
        PreparedStatement psDelDetalles = null;
        PreparedStatement psDelCarrito = null;
        
        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false);

            // 1. Elimina detalles vinculados al carrito activo del usuario
            String sqlDelDetalles = "DELETE FROM DetallesCarrito WHERE Carrito_id IN (SELECT Carrito_id FROM Carrito WHERE Usuarios_id = ?)";
            psDelDetalles = con.prepareStatement(sqlDelDetalles);
            psDelDetalles.setInt(1, idUsuario);
            psDelDetalles.executeUpdate();

            // 2. Elimina la cabecera del carrito
            String sqlDelCarrito = "DELETE FROM Carrito WHERE Usuarios_id = ?";
            psDelCarrito = con.prepareStatement(sqlDelCarrito);
            psDelCarrito.setInt(1, idUsuario);
            psDelCarrito.executeUpdate();

            con.commit();
            System.out.println("🗑️ Limpieza completada: Datos parciales del carrito removidos de la BD por abandono.");
            return true;
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (psDelDetalles != null) psDelDetalles.close();
                if (psDelCarrito != null) psDelCarrito.close();
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}