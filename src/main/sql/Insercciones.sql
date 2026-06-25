USE ModaSv1;

-- Usuarios y roles
INSERT INTO Registro(Registro_id, Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) VALUES 
(1, "JavidAdmin", "Admin123", "juandavidcaceres@gmail.com", "3155746387"),
(2, "JavidUser", "Hola123", "juandavid@gmail.com", "3155746387");

INSERT INTO Permisos_Roles(Permisos_Roles_id, Permisos_Rol, Permisos_Descripcion, Permisos_asignados) VALUES
(1, "Cliente", "Permisos para acceder tanto a las funciones de perfil y carrito de compra", "cliente"),
(2, "Administrador", "permisos para acceder a la interfaz principal de administrador", "admin");

INSERT INTO Usuarios (Registro_id, Permisos_roles_id, Usuario_imagen) VALUES 
(1, 2, "images/Perfil/Ellipse14.png"), 
(2, 1, "images/Perfil/Ellipse14.png");

-- Inserciones de Catalogo / Poulares / Imagenes
INSERT INTO Categoria(Categoria_nombre, Categoria_Descripcion) VALUES 
("Camisas", "Camisas excelentes para salidas ocasionales"),
("Pantalones", "Patalones que se ajustan segun la necesidad del cliente"),
("Pijamas", "Pijamas comodas excelentes para dormir");

-- Mapeado idéntico a los atributos de tu clase Java 'Prendas'
INSERT INTO Prendas(Categoria_id, Prenda_nombre, Prenda_valor, Prenda_talla, Prenda_descripcion, Prenda_stock, Prenda_estado) VALUES 
(1, "Camisa manga larga", 30000.00, "S", "Camisa manga larga ideal para cualquier ocasion", 10, "activa"),
(2, "Pantalon jean", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(3, "Pijama Parte Superior", 30000.00, "M", "Pijama a dormir ", 10, "activa"),
(2, "Pantalon jean largo", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(3, "Pijama completa para  dormir", 30000.00, "M", "Pijama para poder dormir aun mas comodamente", 3, "activa");

INSERT INTO Populares(Prenda_id, Populares_visitas) VALUES 
(1, 30), (2, 20), (3, 25), (4, 20), (5, 25); -- 🔥 Corregido: Valores numéricos reales sin comillas

INSERT INTO imagenes(Prenda_id, Imagenes_link) VALUES 
(1, "/images/Rectangle38.png"), 
(2, "/images/Rectangle38.png"), 
(3, "/images/Rectangle35.png"),
(4, "/images/Rectangle38.png"), 
(5, "/images/Rectangle35.png");


INSERT INTO Registro(Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) VALUES 
("ElenaUser", "Elena789", "elena.gomez@gmail.com", "3219876543");

-- Asociamos el Registro con el Rol de 'Cliente' (Permisos_Roles_id: 1)
-- Esto genera automáticamente el Usuarios_id = 3
INSERT INTO Usuarios (Registro_id, Permisos_roles_id, Usuario_imagen) VALUES 
(3, 1, "images/Perfil/Ellipse 14.png");


-- ====================================================================
-- 2. FLUJO DE PEDIDOS DE CATÁLOGO (JavidUser y ElenaUser)
-- ====================================================================

-- A) Creamos los Carritos (Obligatorio para compras de catálogo)
-- Carrito 1 para JavidUser (Usuarios_id: 2) y Carrito 2 para ElenaUser (Usuarios_id: 3)
INSERT INTO Carrito(Carrito_id, Usuarios_id, Carrito_fechaCreacion, Carrito_Estado) VALUES 
(1, 2, '2026-06-20', 'procesado'),
(2, 3, '2026-06-21', 'procesado');

-- B) Agregamos las prendas a los detalles del carrito
-- Javid compra 2 'Camisas manga larga' (Prenda_id: 1, Valor: 30000 -> Total: 60000)
-- Elena compra 1 'Pantalon jean' (Prenda_id: 2, Valor: 30000 -> Total: 30000)
INSERT INTO DetallesCarrito(DetallesCarrito_Id, Prendas_id, Carrito_id, Detalles_cantidad) VALUES 
(1, 1, 1, 2),
(2, 2, 2, 1);


-- ====================================================================
-- 3. FLUJO DE PEDIDOS A MEDIDA / PERSONALIZADOS (JavidUser y ElenaUser)
-- ====================================================================

-- A) Los usuarios registran sus solicitudes de diseño desde cero
INSERT INTO DetallesPedidosMedida(Detalles_PedidoMedida_id, Usuario_id, Detalles_medidas, Detalles_TPrenda, Detalles_ImagenReferencia, Detalles_Tela, Detalles_Descripcion) VALUES 
(1, 2, 'Pecho: 95cm, Cintura: 85cm, Largo: 72cm', 'Chaqueta de Gala', '/images/Rectangle35.png', 'Paño', 'Chaqueta elegante con costuras ocultas y botones dorados.'),
(2, 3, 'Cintura: 78cm, Cadera: 96cm, Largo: 102cm', 'Vestido Casual', '/images/Rectangle35.png', 'Lino', 'Vestido corto y fresco color crema ideal para clima cálido.');

-- B) El Sastre (Admin) responde con la cotización económica de los diseños
INSERT INTO CotizacionPedido(CotizacionPedido_Id, DetallesPedidosMedida_id, Cotizacion_Valor, ComentarioAdmin, Cotizacion_FechaLimite) VALUES 
(1, 1, 150000.00, 'Material disponible en taller. Mano de obra alta por detalles de botones.', '2026-07-15'),
(2, 2, 110000.00, 'Lino importado de alta calidad. Tiempo estimado de confección: 4 días.', '2026-07-20');


-- ====================================================================
-- 4. ENCABEZADOS DE PEDIDOS (La orden de compra unificada)
-- ====================================================================
-- Insertamos los 4 registros de compra definitivos
INSERT INTO Pedidos(Pedido_id, Pedido_TipoPedido, Pedido_MetodoPago, Pedido_FechaInicio, Pedido_TelefonoContacto, Pedido_Direccion, Pedido_Estado, Pedido_TotalCompra) VALUES 
-- Pedidos de JavidUser
(1, 'Catalogo', 'Nequi', '2026-06-21', '3155746387', 'Calle 10 #5-20, Giron', 'Entregado', 60000.00),
(2, 'A Medida', 'Daviplata', '2026-06-22', '3155746387', 'Calle 10 #5-20, Giron', 'En proceso', 150000.00),
-- Pedidos de ElenaUser
(3, 'Catalogo', 'Efectivo', '2026-06-21', '3219876543', 'Av. Central #12-45, Bucaramanga', 'Pendiente', 30000.00),
(4, 'A Medida', 'Nequi', '2026-06-22', '3219876543', 'Av. Central #12-45, Bucaramanga', 'Aceptado y Pagado', 110000.00);


-- ====================================================================
-- 5. RELACIÓN FINAL (DetallesPedidos)
-- ====================================================================
-- Aquí cruzamos cada Pedido con su respectivo origen (Carrito o Cotización)
INSERT INTO DetallesPedidos(DetallesPedidos_id, Pedido_id, DetallesCarrito_id, CotizacionPedido_id, Detalles_PrecioTotal) VALUES 
-- Vinculación para JavidUser
(1, 1, 1, NULL, 60000.00),  -- Pedido 1 apunta al DetallesCarrito 1
(2, 2, NULL, 1, 150000.00), -- Pedido 2 apunta a la CotizacionPedido 1

-- Vinculación para ElenaUser
(3, 3, 2, NULL, 30000.00),  -- Pedido 3 apunta al DetallesCarrito 2
(4, 4, NULL, 2, 110000.00); -- Pedido 4 apunta a la CotizacionPedido 2

