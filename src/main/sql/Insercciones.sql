USE ModaS;

-- ================================================
-- 1. USUARIOS Y ROLES (Tus datos exactos)
-- ================================================
insert into Registro(Registro_id, Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) values 
(1, "JavidAdmin", "Admin123", "juandavidcaceres@gmail.com", "3155746387"),
(2, "JavidUser", "Hola123", "juandavid@gmail.com", "3155746387");

insert into Permisos_Roles(Permisos_Roles_id, Permisos_Rol, Permisos_Descripcion, Permisos_asignados) values
(1, "Cliente", "Permisos para acceder tanto a las funciones de perfil y carrito de compra", "cliente"),
USE ModaS;

-- ================================================
-- 1. USUARIOS Y ROLES (Rutas web corregidas con /)
-- ================================================
INSERT INTO Registro(Registro_id, Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) VALUES 
(1, "JavidAdmin", "Admin123", "juandavidcaceres@gmail.com", "3155746387"),
(2, "JavidUser", "Hola123", "juandavid@gmail.com", "3155746387");

INSERT INTO Permisos_Roles(Permisos_Roles_id, Permisos_Rol, Permisos_Descripcion, Permisos_asignados) VALUES
(1, "Cliente", "Permisos para acceder tanto a las funciones de perfil y carrito de compra", "cliente"),
(2, "Administrador", "permisos para acceder a la interfaz principal de administrador", "admin");

INSERT INTO Usuarios (Usuarios_id, Registro_id, Permisos_roles_id, Usuario_imagen, Usuario_Medidas) VALUES 
(1, 1, 2, "images/Perfil/Ellipse 14.png", ""), -- 🔥 Corregido: Barra invertida eliminada
(2, 2, 1, "images/Perfil/Ellipse 14.png", "Cintura: 84cm, Espalda: 48cm"); -- 🔥 Corregido: Barra invertida eliminada

-- ================================================
-- 2. CATÁLOGO, POPULARES E IMÁGENES (Campos estables y enteros)
-- ================================================
INSERT INTO Categoria(Categoria_id, Categoria_nombre, Categoria_Descripcion) VALUES 
(1, "Camisas", "Camisas excelentes para salidas ocasionales"),
(2, "Pantalones", "Patalones que se ajustan segun la necesidad del cliente"),
(3, "Pijamas", "Pijamas comodas excelentes para dormir");

-- Mapeado idéntico a los atributos de tu clase Java 'Prendas'
INSERT INTO Prendas(Prenda_id, Categoria_id, Prenda_nombre, Prenda_precio, Prenda_talla, Prenda_descripcion, Prenda_stock, Prenda_estado) VALUES 
(1, 1, "Camisa manga larga", 30000.00, "S", "Camisa manga larga ideal para cualquier ocasion", 10, "activa"),
(2, 2, "Pantalon jean", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(3, 3, "Pijama completa", 30000.00, "M", "Pijama para poder dormir aun mas comodamente", 10, "activa"),
(4, 2, "Pantalon jean", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(5, 3, "Pijama completa pa dormir", 30000.00, "M", "Pijama para poder dormir aun mas comodamente", 3, "activa");

INSERT INTO Populares(Prenda_id, Populares_visitas) VALUES 
(1, 30), (2, 20), (3, 25), (4, 20), (5, 25); -- 🔥 Corregido: Valores numéricos reales sin comillas

INSERT INTO imagenes(Prenda_id, Imagenes_link) VALUES 
(1, "images/catalogo/Rectangle_38.png"), -- 🔥 Recomendado: Rutas relativas sin / inicial si manejas urlBase en JS
(2, "images/catalogo/Rectangle_38.png"), 
(3, "images/catalogo/Rectangle_35.png"),
(4, "images/catalogo/Rectangle_38.png"), 
(5, "images/catalogo/Rectangle_35.png");


-- ================================================
-- 3. FLUJO DE CATÁLOGO (Consistencia completa)
-- ================================================
INSERT INTO Carrito (Carrito_id, Usuarios_id, Carrito_fecha) VALUES 
(1, 2, '2026-06-04'), 
(2, 2, '2026-06-02'), 
(3, 2, '2026-05-15'); 

INSERT INTO DetallesCarrito (DetallesCarrito_Id, Prendas_id, Carrito_id, Detalles_total) VALUES 
(1, 1, 1, 30000.00),  
(2, 2, 2, 30000.00),  
(3, 3, 3, 30000.00);


-- ================================================
-- 4. FLUJO DE PEDIDOS A MEDIDA (Excelente arquitectura aislada)
-- ================================================
INSERT INTO DetallesPedidosMedida (Detalles_PedidoMedida_id, Usuario_id, Detalles_medidas, Detalles_TPrenda, Detalles_ImagenReferencia, Detalles_Tela, Detalles_Descripcion) VALUES 
(1, 2, 'Cuello: 40cm, Mangas: 65cm, Espalda: 48cm', 'Traje Formal', 'images/referencias/Rectangle_38.png', 'Paño', 'Traje de gala para grado académico'),
(2, 2, 'Cintura: 84cm, Largo: 102cm', 'Vestido Casual', 'images/referencias/Rectangle_38.png', 'Lino', 'Vestido veraniego suelto con botones');

INSERT INTO CotizacionPedido (CotizacionPedido_Id, DetallesPedidosMedida_id, Cotizacion_Valor, ComentarioAdmin, Cotizacion_FechaLimite) VALUES 
(1, 1, 450000.00, 'Aceptado, se inicia tras el pago', '2026-06-15'),
(2, 2, 180000.00, 'Diseño aprobado por el sastre', '2026-06-20');


-- ================================================
-- 5. CONFIRMAR PAGO (Control de nulos para auditoría)
-- ================================================
INSERT INTO ConfirmarPago (ConfirmarPago_id, CotizacionPedido_id, DetallesCarrito_id, ConfirmarPago_TipoPedido, ConfirmarPago_MetodoP, ConfirmarPago_Fecha, ConfirmarTelefono) VALUES 
(1, 1, NULL, 'Medida', 'Nequi', '2026-06-01', '3155746387'),    
(2, 2, NULL, 'Medida', 'Daviplata', '2026-06-04', '3155746387'),  
(3, NULL, 1, 'Catalogo', 'Nequi', '2026-06-04', '3155746387'),    
(4, NULL, 2, 'Catalogo', 'Daviplata', '2026-06-02', '3155746387'),  
(5, NULL, 3, 'Catalogo', 'Nequi', '2026-05-15', '3155746387');  


-- ================================================
-- 6. PEDIDOS EN TRABAJO (Sincronizado con estados de ServeltAdminPedidos)
-- ================================================
INSERT INTO Pedidos (Pedido_id, ConfirmarPago_id, Pedido_FechaInicio, Pedido_Estado, Pedido_Direcccion, Pedido_TCompra) VALUES 
(1, 1, '2026-06-01', 'Elaboracion', 'Calle 10 # 5-20 Centro', 'Medida'),
(2, 2, '2026-06-04', 'Pendiente', 'Carrera 15 # 24-32 Norte', 'Medida'),
(3, 3, '2026-06-04', 'Pendiente', 'Calle 10 # 5-20 Centro', 'Catalogo'),
(4, 4, '2026-06-02', 'Elaboracion', 'Calle 10 # 5-20 Centro', 'Catalogo'),
(5, 5, '2026-05-15', 'Por Entregar', 'Carrera 15 # 24-32 Norte', 'Catalogo');


-- ================================================
-- 7. HISTORIAL DE PAGOS 
-- ================================================
INSERT INTO HistorialPagos (ConfirmarPago_id, Historial_Fecha) VALUES 
(1, '2026-06-01'), 
(2, '2026-06-04'), 
(3, '2026-06-04'), 
(4, '2026-06-02'), 
(5, '2026-05-15');
