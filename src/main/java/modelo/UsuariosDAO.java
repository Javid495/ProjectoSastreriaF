package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Dtos.IniciarSesion;
import Dtos.PrendasRecientes;
import Dtos.Pedidos;

public class UsuariosDAO {


    // Metodo que busca los datos del usuario
    public IniciarSesion obtenerDatosUsuario(int usuarioId) {
        
       
        IniciarSesion user = null;
        
        //La siguiente consulta busca los datos del usuario en la tabla usuarios y registro
        String sql = "SELECT u.Usuarios_id, r.Registro_Usuario, r.Registro_Email, r.Registro_Telefono, u.Usuario_imagen, u.Permisos_roles_id " +
                     "FROM Usuarios u JOIN Registro r ON u.Registro_id = r.Registro_id WHERE u.Usuarios_id = ?";
        
        //En el bloque try
        //Preparamos la conexion ccon la base de datos
        //preparamos la insericion
        //Y asignamos en el ? el id que viene del servelt
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            
            //Ejecutamos la consulta
            try (ResultSet rs = ps.executeQuery()) {
                
                //Verificamos si nos trae daos del usuario
                if (rs.next()) {
                    
                     //Del dto de iniciarsesion nos traemos el objecto declara do de iniciarsesion
                    user = new IniciarSesion();
                    
                    user.setId(rs.getInt("Usuarios_id"));
                    user.setUsuario(rs.getString("Registro_Usuario"));
                    user.setEmail(rs.getString("Registro_Email"));
                    user.setTelefono(rs.getString("Registro_Telefono"));
                    user.setImagen(rs.getString("Usuario_imagen"));
                    user.setRolUsuario(rs.getInt("Permisos_roles_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // 🏷️ 2. BUSCAR PRENDAS RECIENTES
    public List<PrendasRecientes> obtenerPrendasRecientes(int usuarioId) {
        List<PrendasRecientes> lista = new ArrayList<>();
        String sql = "SELECT p.Prenda_id, p.Prenda_nombre, p.Prenda_valor, " +
                     "(SELECT i.Imagenes_link FROM imagenes i WHERE i.Prenda_id = p.Prenda_id LIMIT 1) AS img " +
                     "FROM Historial_PrendasRecientes h " +
                     "JOIN Prendas p ON h.Id_Prenda = p.Prenda_id " +
                     "WHERE h.Id_Usuarios = ? ORDER BY h.Id_Historial DESC LIMIT 4";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, usuarioId);
             
            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    String rutaImg = rs.getString("img");
                    if (rutaImg == null) rutaImg = "images/Prendas/prenda_b.jpg";

                    lista.add(new PrendasRecientes(
                        rs.getInt("Prenda_id"),
                        rs.getString("Prenda_nombre"),
                        rs.getDouble("Prenda_valor"),
                        rutaImg
                    ));
                }
            }
        } 
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return lista;
    }

    // 📦 3. BUSCAR HISTORIAL DE PEDIDOS (Adaptado a tus getters y setters reales)
    public List<Pedidos> obtenerHistorialPedidos(int usuarioId) {
        List<Pedidos> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT p.Pedido_id, p.Pedido_TipoPedido, p.Pedido_FechaInicio, p.Pedido_Estado, p.Pedido_Direccion " +
                     "FROM Pedidos p " +
                     "JOIN DetallesPedidos dp ON p.Pedido_id = dp.Pedido_id " +
                     "LEFT JOIN DetallesCarrito dc ON dp.DetallesCarrito_id = dc.DetallesCarrito_Id " +
                     "LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id " +
                     "LEFT JOIN CotizacionPedido cp ON dp.CotizacionPedido_id = cp.CotizacionPedido_Id " +
                     "LEFT JOIN DetallesPedidosMedida dpm ON cp.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id " +
                     "WHERE c.Usuarios_id = ? OR dpm.Usuario_id = ? " +
                     "ORDER BY p.Pedido_FechaInicio DESC";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, usuarioId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedidos p = new Pedidos();
                    // Usamos tus nombres de métodos exactos:
                    p.setIdPedido(rs.getInt("Pedido_id"));
                    p.setTipoCompra(rs.getString("Pedido_TipoPedido"));
                    p.setFechaInicio(rs.getString("Pedido_FechaInicio"));
                    p.setEstadoPedido(rs.getString("Pedido_Estado"));
                    p.setDireccion(rs.getString("Pedido_Direccion"));

                    lista.add(p);
                }
            }
        } 
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        
        return lista;
    }

   // 💾 4. ACTUALIZAR LOS DATOS (Sincronizado con el Formulario e Imagen del Perfil)
    public boolean actualizarPerfil(int usuarioId, String nombre, String telefono, String correo, String imagenAvatar) {
        // 🌟 Añadimos u.Usuario_imagen al UPDATE integrado
        String sql = "UPDATE Registro r JOIN Usuarios u ON r.Registro_id = u.Registro_id " +
                     "SET r.Registro_Usuario = ?, r.Registro_Telefono = ?, r.Registro_Email = ?, u.Usuario_imagen = ? WHERE u.Usuarios_id = ?";
        
        
        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setString(3, correo);
            ps.setString(4, imagenAvatar); 
            ps.setInt(5, usuarioId);

            return ps.executeUpdate() > 0;
        }
        
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
        public boolean registrarPrendaReciente(int usuarioId, int prendaId) {
        // Añadimos la columna Historial_Fecha y usamos CURDATE() para que MySQL ponga la fecha de hoy
        String sql = "INSERT INTO Historial_PrendasRecientes (Id_Usuarios, Id_Prenda, Historial_Fecha) VALUES (?, ?, CURDATE())";

        try (Connection con = ClaseConexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, prendaId);

            System.out.println("📌 [UsuariosDAO] Registrando prenda " + prendaId + " en el historial del usuario " + usuarioId + " con fecha de hoy.");
            
            //Retorna un booleano si se realizo una inserccion
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ [UsuariosDAO] Error al registrar prenda reciente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
        
    // Metodo de listar usuarios para Admin
   public List<IniciarSesion> listarTodosLosUsuarios() {
    List<IniciarSesion> lista = new ArrayList<>();
    
    // Hacemos el INNER JOIN para fusionar los datos de Registro con la tabla Usuarios
    String sql = "SELECT u.Usuarios_id, r.Registro_Usuario, r.Registro_Email, r.Registro_Telefono, u.Usuario_imagen "
               + "FROM Usuarios u "
               + "INNER JOIN Registro r ON u.Registro_id = r.Registro_id "
               + "WHERE u.Permisos_roles_id = 1 "
               + "ORDER BY r.Registro_Usuario ASC";

    try (Connection con = ClaseConexion.getConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            IniciarSesion usuario = new IniciarSesion();
            
            // ATENCIÓN: Guardamos 'Usuarios_id' porque este es el ID que amarra las compras y los logs de auditoría
            usuario.setId(rs.getInt("Usuarios_id"));
            usuario.setUsuario(rs.getString("Registro_Usuario"));
            usuario.setEmail(rs.getString("Registro_Email"));
            usuario.setTelefono(rs.getString("Registro_Telefono"));
            usuario.setImagen(rs.getString("Usuario_imagen"));

            lista.add(usuario);
        }
    } 
    
    catch (SQLException e) {
        System.err.println("Error al listar usuarios con JOIN en UsuariosDAO: " + e.getMessage());
    }
        return lista;
    }
   
   
   
}
