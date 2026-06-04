USE ModaS;

-- ================================================
-- 1. USUARIOS Y ROLES (Tus datos exactos)
-- ================================================
insert into Registro(Registro_id, Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) values 
(1, "JavidAdmin", "Admin123", "juandavidcaceres@gmail.com", "3155746387"),
(2, "JavidUser", "Hola123", "juandavid@gmail.com", "3155746387");

insert into Permisos_Roles(Permisos_Roles_id, Permisos_Rol, Permisos_Descripcion, Permisos_asignados) values
(1, "Cliente", "Permisos para acceder tanto a las funciones de perfil y carrito de compra", "cliente"),
(2, "Administrador", "permisos para acceder a la interfaz principal de administrador", "admin");

insert into Usuarios (Usuarios_id, Registro_id, Permisos_roles_id, Usuario_imagen, Usuario_Medidas) values 
(1, 1, 2, "images\\Perfil\\Ellipse 14.png", ""),
(2, 2, 1, "images\\Perfil\\Ellipse 14.png", "Cintura: 84cm, Espalda: 48cm");

-- ================================================
-- 2. CATÁLOGO, POPULARES E IMÁGENES (Tus datos exactos)
-- ================================================
insert into Categoria(Categoria_id, Categoria_nombre, Categoria_Descripcion) values 
(1, "Camisas", "Camisas excelentes para salidas ocasionales"),
(2, "Pantalones", "Patalones que se ajustan segun la necesidad del cliente"),
(3, "Pijamas", "Pijamas comodas excelentes para dormir");

insert into Prendas(Prenda_id, Categoria_id, Prenda_nombre, Prenda_valor, Prenda_talla, Prenda_descripcion, Prenda_stock, Prenda_estado) values 
(1, 1, "Camisa manga larga", 30000.00, "S", "Camisa manga larga ideal para cualquier ocasion", 10, "activa"),
(2, 2, "Pantalon jean", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(3, 3, "Pijama completa", 30000.00, "M", "Pijama para poder dormir aun mas comodamente", 10, "activa"),
(4, 2, "Pantalon jean", 30000.00, "16", "Pantalon jean ideal para salidas casuales", 10, "activa"),
(5, 3, "Pijama completa pa dormir", 30000.00, "M", "Pijama para poder dormir aun mas comodamente", 3, "activa");

insert into Populares(Prenda_id, Populares_visitas) values 
(1, "30"), (2, "20"), (3, "25"),(4, "20"), (5, "25");

insert into imagenes(Prenda_id, Imagenes_link) values 
(1, "/images/Rectangle 38.png"),
(2, "/images/Rectangle 38.png"), 
(3, "/images/Rectangle 35.png"),
(4, "/images/Rectangle 38.png"), 
(5, "/images/Rectangle 35.png");


-- ================================================
-- 3. FLUJO DE CATÁLOGO (Para completar los 5 registros)
-- ================================================
-- Carritos creados para JavidUser (Usuario_id: 2)
insert into Carrito (Carrito_id, Usuarios_id, Carrito_fecha) values 
(1, 2, '2026-06-04'), -- Compra de hoy
(2, 2, '2026-06-02'), -- Compra de esta semana
(3, 2, '2026-05-15'); -- Compra del mes pasado (Histórico)

insert into DetallesCarrito (DetallesCarrito_Id, Prendas_id, Carrito_id, Detalles_total) values 
(1, 1, 1, 30000.00),  -- Camisa manga larga
(2, 2, 2, 30000.00),  -- Pantalon jean
(3, 3, 3, 30000.00);  -- Pijama completa


-- ================================================
-- 4. FLUJO DE PEDIDOS A MEDIDA (Corregido según tu Schema)
-- ================================================
-- Registro de los diseños desde cero solicitados por JavidUser
insert into DetallesPedidosMedida (Detalles_PedidoMedida_id, Usuario_id, Detalles_medidas, Detalles_TPrenda, Detalles_ImagenReferencia, Detalles_Tela, Detalles_Descripcion) values 
(1, 2, 'Cuello: 40cm, Mangas: 65cm, Espalda: 48cm', 'Traje Formal', '/images/Rectangle 38.png', 'Paño', 'Traje de gala para grado académico'),
(2, 2, 'Cintura: 84cm, Largo: 102cm', 'Vestido Casual', '/images/Rectangle 38.png', 'Lino', 'Vestido veraniego suelto con botones');

-- El administrador aprueba y define los costos en la tabla CotizacionPedido
insert into CotizacionPedido (CotizacionPedido_Id, DetallesPedidosMedida_id, Cotizacion_Valor, ComentarioAdmin, Cotizacion_FechaLimite) values 
(1, 1, 450000.00, 'Aceptado, se inicia tras el pago', '2026-06-15'),
(2, 2, 180000.00, 'Diseño aprobado por el sastre', '2026-06-20');


-- ================================================
-- 5. CONFIRMAR PAGO (Exactamente 5 registros con Nequi y Daviplata)
-- ================================================
insert into ConfirmarPago (ConfirmarPago_id, CotizacionPedido_id, DetallesCarrito_id, ConfirmarPago_TipoPedido, ConfirmarPago_MetodoP, ConfirmarPago_Fecha, ConfirmarTelefono) values 
(1, 1, NULL, 'Medida', 'Nequi', '2026-06-01', '3155746387'),     -- Pedido Medida 1 (Esta semana/Este mes) -> $450.000
(2, 2, NULL, 'Medida', 'Daviplata', '2026-06-04', '3155746387'),  -- Pedido Medida 2 (Hoy/Esta semana/Este mes) -> $180.000
(3, NULL, 1, 'Catalogo', 'Nequi', '2026-06-04', '3155746387'),     -- Catálogo Camisa (Hoy/Esta semana/Este mes) -> $30.000
(4, NULL, 2, 'Catalogo', 'Daviplata', '2026-06-02', '3155746387'),  -- Catálogo Pantalón (Esta semana/Este mes) -> $30.000
(5, NULL, 3, 'Catalogo', 'Nequi', '2026-05-15', '3155746387');     -- Catálogo Pijama (Mes pasado) -> $30.000


-- ================================================
-- 6. PEDIDOS EN TRABAJO (Estados: Pendiente, Elaboracion, Por Entregar)
-- ================================================
insert into Pedidos (Pedido_id, ConfirmarPago_id, Pedido_FechaInicio, Pedido_Estado, Pedido_Direcccion, Pedido_TCompra) values 
(1, 1, '2026-06-01', 'Elaboracion', 'Calle 10 # 5-20 Centro', 'Medida'),
(2, 2, '2026-06-04', 'Pendiente', 'Carrera 15 # 24-32 Norte', 'Medida'),
(3, 3, '2026-06-04', 'Pendiente', 'Calle 10 # 5-20 Centro', 'Catalogo'),
(4, 4, '2026-06-02', 'Elaboracion', 'Calle 10 # 5-20 Centro', 'Catalogo'),
(5, 5, '2026-05-15', 'Por Entregar', 'Carrera 15 # 24-32 Norte', 'Catalogo');


-- ================================================
-- 7. HISTORIAL DE PAGOS (Esencial para que tu DAO devuelva datos)
-- ================================================
insert into HistorialPagos (ConfirmarPago_id, Historial_Fecha) values 
(1, '2026-06-01'), -- Traje a medida ($450.000)
(2, '2026-06-04'), -- Vestido a medida ($180.000)
(3, '2026-06-04'), -- Camisa catálogo ($30.000)
(4, '2026-06-02'), -- Pantalón catálogo ($30.000)
(5, '2026-05-15'); -- Pijama catálogo ($30.000)
