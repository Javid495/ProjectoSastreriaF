package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

// DAO que maneja el proceso y el flujo de compra respetando el diseño original de la BD
public class CompraPedidosDAO {

    // ==========================================================================
    // 🛒 OPERACIÓN TEMPORAL: REGISTRAR O ACTUALIZAR CARRITO
    // ==========================================================================
    public boolean registrarCarritoTemporal(int idUsuario, List<int[]> listaProductos) {
        Connection con = null; 
        PreparedStatement psCarrito = null; 
        PreparedStatement psDetalleCar = null;
        ResultSet rs = null;

        try {
            // Pasamos los carritos 'activos' anteriores a 'abandonados' en vez de borrarlos
            limpiarCarritoAbandono(idUsuario);

            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); 

            // 1. Inserción en 'Carrito'
            String sqlCarrito = "INSERT INTO Carrito (Usuarios_id, Carrito_fechaCreacion) VALUES (?, CURDATE())";
            psCarrito = con.prepareStatement(sqlCarrito, Statement.RETURN_GENERATED_KEYS);
            psCarrito.setInt(1, idUsuario);
            psCarrito.executeUpdate();

            int idCarritoGenerado = 0;
            rs = psCarrito.getGeneratedKeys();
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
        
            psDetalleCar.executeBatch(); 
            con.commit(); 
            System.out.println("📌 [CompraDAO] Carrito activo creado con ID: " + idCarritoGenerado);
            return true;

        } 
        
        catch (Exception e) {
            System.out.println("❌ Error al registrar el carrito temporal: " + e.getMessage());
            if (con != null) {
                
                
                try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } 
        
        finally {
            try {
                if (rs != null) rs.close();
                if (psCarrito != null) psCarrito.close();
                if (psDetalleCar != null) psDetalleCar.close();
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    
    // ==========================================================================
    // 🛍️ FLUJO 1: REGISTRAR COMPRA DESDE EL CARRITO DE CATÁLOGO
    // ==========================================================================
    public boolean confirmarPedidoDefinitivo(int idUsuario, String direccion, String telefono, 
                                             String metodoPago, String tipoPedido, List<int[]> listaProductos) {
        Connection con = null; 
        PreparedStatement psGetCarrito = null;
        PreparedStatement psPedido = null;
        PreparedStatement psGetDetalleId = null;
        PreparedStatement psDetallePed = null;
        PreparedStatement psStock = null;  
        PreparedStatement psCerrarCarrito = null;
        ResultSet rsCar = null;
        ResultSet rsPed = null;
        ResultSet rsDetId = null; // 🌟 Movido al bloque seguro para evitar fugas de memoria

        try {
            con = ClaseConexion.getConexion();
            con.setAutoCommit(false); 

            // 1. Buscar cuál es su carrito activo actual
            String sqlGetCarrito = "SELECT Carrito_id FROM Carrito WHERE Usuarios_id = ? AND Carrito_Estado = 'activo' ORDER BY Carrito_id DESC LIMIT 1";
            psGetCarrito = con.prepareStatement(sqlGetCarrito);
            psGetCarrito.setInt(1, idUsuario);
            rsCar = psGetCarrito.executeQuery();
            
            int idCarritoActivo = 0;
            if (rsCar.next()) {
                idCarritoActivo = rsCar.getInt("Carrito_id");
            } else {
                throw new Exception("No se encontró ningún carrito activo para procesar la compra.");
            }

            // 2. Calcular el total de la compra
            double totalCompra = 0;
            for (int[] prod : listaProductos) {
                totalCompra += prod[2]; 
            }

            // 3. Inserción en 'Pedidos'
            String sqlPedido = "INSERT INTO Pedidos (Pedido_TipoPedido, Pedido_MetodoPago, Pedido_FechaInicio, "
                             + "Pedido_TelefonoContacto, Pedido_Direccion, Pedido_Estado, Pedido_TotalCompra) "
                             + "VALUES (?, ?, CURDATE(), ?, ?, 'Pendiente', ?)";
            
            psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setString(1, tipoPedido);
            psPedido.setString(2, metodoPago);
            psPedido.setString(3, telefono);
            psPedido.setString(4, direccion);
            psPedido.setDouble(5, totalCompra);
            psPedido.executeUpdate();

            int idPedidoGenerado = 0;
            rsPed = psPedido.getGeneratedKeys();
            if (rsPed.next()) {
                idPedidoGenerado = rsPed.getInt(1);
            }

            // 4. Llenar DetallesPedidos enlazando al DetallesCarrito_id correspondiente
            String sqlDetallePed = "INSERT INTO DetallesPedidos (Pedido_id, DetallesCarrito_id, CotizacionPedido_id, Detalles_PrecioTotal) "
                                 + "VALUES (?, ?, NULL, ?)";
            psDetallePed = con.prepareStatement(sqlDetallePed);

            String sqlStock = "UPDATE Prendas SET Prenda_stock = Prenda_stock - ? WHERE Prenda_id = ?";
            psStock = con.prepareStatement(sqlStock);

            String sqlGetDetalleId = "SELECT DetallesCarrito_Id FROM DetallesCarrito WHERE Carrito_id = ? AND Prendas_id = ?";
            psGetDetalleId = con.prepareStatement(sqlGetDetalleId);

            for (int[] prod : listaProductos) {
                int idPrenda = prod[0];
                int cantidad = prod[1];      
                double totalLinea = prod[2];  

                psGetDetalleId.setInt(1, idCarritoActivo);
                psGetDetalleId.setInt(2, idPrenda);
                rsDetId = psGetDetalleId.executeQuery();
                
                int idDetalleCarrito = 0;
                if (rsDetId.next()) {
                    idDetalleCarrito = rsDetId.getInt("DetallesCarrito_Id");
                }
                rsDetId.close(); // Se cierra tras su uso en ciclo

                if (idDetalleCarrito > 0) {
                    psDetallePed.setInt(1, idPedidoGenerado);
                    psDetallePed.setInt(2, idDetalleCarrito);
                    psDetallePed.setDouble(3, totalLinea);
                    psDetallePed.addBatch();
                }

                psStock.setInt(1, cantidad);  
                psStock.setInt(2, idPrenda);  
                psStock.addBatch();
            }
            psDetallePed.executeBatch();
            psStock.executeBatch();

            // 5. El carrito pasa a estar 'comprado'
            String sqlCerrarCarrito = "UPDATE Carrito SET Carrito_Estado = 'comprado' WHERE Carrito_id = ?";
            psCerrarCarrito = con.prepareStatement(sqlCerrarCarrito);
            psCerrarCarrito.setInt(1, idCarritoActivo);
            psCerrarCarrito.executeUpdate();

            // 🌟 6. Escribir en el historial compartiendo de forma segura la misma transacción ('con')
            HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
            historialDAO.registrarAccion(
                con, // 👈 Pasamos la conexión activa aquí
                idUsuario,
                "COMPRA_CATALOGO",
                "Pedidos",
                idPedidoGenerado, 
                "El usuario confirmó y pagó una compra de prendas desde el catálogo estándar."
            );

            con.commit(); 
            System.out.println("🚀 Pedido #" + idPedidoGenerado + " registrado con éxito usando relaciones normalizadas.");
            return true;

        } 
        
        catch (Exception e) {
            System.out.println("❌ Error al confirmar el pedido final: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } 
        
        finally {
            try {
                if (rsCar != null) rsCar.close();
                if (rsPed != null) rsPed.close();
                if (rsDetId != null) rsDetId.close(); // 🌟 Cierre preventivo
                if (psGetCarrito != null) psGetCarrito.close();
                if (psPedido != null) psPedido.close();
                if (psGetDetalleId != null) psGetDetalleId.close();
                if (psDetallePed != null) psDetallePed.close();
                if (psStock != null) psStock.close(); 
                if (psCerrarCarrito != null) psCerrarCarrito.close();
                if (con != null) con.close();
            } 
            
            catch (Exception e) { e.printStackTrace(); }
        }
    }
    
    // ==========================================================================
    // 🧵 FLUJO 2: CONFIRMAR PEDIDO A MEDIDA DEFINITIVO (Desde Cotización)
    // ==========================================================================
    public boolean confirmarPedidoAMedidaDefinitivo(int idUsuario, String direccion, String telefono, 
                                                    String metodoPago, String tipoPedido, int idCotizacion) {
        Connection con = null;
        PreparedStatement psGetValor = null;
        PreparedStatement psPedido = null;
        PreparedStatement psDetallePed = null;
        ResultSet rs = null;
        ResultSet rsPed = null;

        try {
            con = ClaseConexion.getConexion(); 
            con.setAutoCommit(false); 

            // 1. Consultar el valor de la cotización aprobada
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

            // 2. Inserción en 'Pedidos'
            String sqlPedido = "INSERT INTO Pedidos (Pedido_TipoPedido, Pedido_MetodoPago, Pedido_FechaInicio, "
                             + "Pedido_TelefonoContacto, Pedido_Direccion, Pedido_Estado, Pedido_TotalCompra) "
                             + "VALUES (?, ?, CURDATE(), ?, ?, 'Pendiente', ?)";
        
            psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setString(1, tipoPedido);
            psPedido.setString(2, metodoPago);
            psPedido.setString(3, telefono);
            psPedido.setString(4, direccion);
            psPedido.setDouble(5, totalCompra);
            psPedido.executeUpdate();

            int idPedidoGenerado = 0;
            rsPed = psPedido.getGeneratedKeys();
            if (rsPed.next()) {
                idPedidoGenerado = rsPed.getInt(1);
            }

            // 3. Vincular el pedido con la cotización
            String sqlDetallePed = "INSERT INTO DetallesPedidos (Pedido_id, DetallesCarrito_id, CotizacionPedido_id, Detalles_PrecioTotal) "
                                 + "VALUES (?, NULL, ?, ?)";
            
            psDetallePed = con.prepareStatement(sqlDetallePed);
            psDetallePed.setInt(1, idPedidoGenerado);
            psDetallePed.setInt(2, idCotizacion);
            psDetallePed.setDouble(3, totalCompra);
            psDetallePed.executeUpdate();
            
            // 🌟 4. Escribir en el historial compartiendo de forma segura la misma transacción ('con')
            HistorialUsuarioDAO historialDAO = new HistorialUsuarioDAO();
            historialDAO.registrarAccion(
                con, // 👈 Pasamos la conexión activa aquí
                idUsuario,
                "COMPRA_MEDIDA",
                "Pedidos",
                idPedidoGenerado,
                "El usuario aceptó la cotización y confirmó el pago de su pedido hecho desde cero."
            );
            
            con.commit(); 
            System.out.println("🧵 Éxito: Cotización #" + idCotizacion + " convertida en Pedido #" + idPedidoGenerado + " sin alterar el esquema.");
            return true;

        } 
        
        catch (Exception e) {
            System.out.println("❌ Error en la confirmation del Pedido a Medida: " + e.getMessage());
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
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    //Cambiar estados de carrito como abandonados
    public boolean limpiarCarritoAbandono(int idUsuario) {
        Connection con = null;
        PreparedStatement psUpdateCarrito = null;
        
        try {
            con = ClaseConexion.getConexion();
            
            String sqlUpdateCarrito = "UPDATE Carrito SET Carrito_Estado = 'abandonado' WHERE Usuarios_id = ? AND Carrito_Estado = 'activo'";
            psUpdateCarrito = con.prepareStatement(sqlUpdateCarrito);
            psUpdateCarrito.setInt(1, idUsuario);
            psUpdateCarrito.executeUpdate();
            
            return true;
        } 
        
        catch (Exception e) {
            System.out.println("❌ Error en borrado lógico del carrito: " + e.getMessage());
            return false;
        } 
        
        finally {
            try {
                if (psUpdateCarrito != null) psUpdateCarrito.close();
                if (con != null) con.close();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}