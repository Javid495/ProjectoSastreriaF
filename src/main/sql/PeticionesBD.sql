use ModaS;

-- Tablas Generales
SELECT * FROM Registro;
SELECT * FROM Permisos_Roles;
SELECT * FROM Usuarios;
SELECT * FROM Categoria;
SELECT * FROM Prendas;
SELECT * FROM Populares;
SELECT * FROM imagenes;
SELECT * FROM Carrito; 
SELECT * FROM DetallesCarrito;
SELECT * FROM DetallesPedidosMedida;
SELECT * FROM CotizacionPedido;
SELECT * FROM ConfirmarPago;
SELECT * FROM Pedidos;
SELECT * FROM HistorialPagos;


-- para verificar los datos de registro de usuario
SELECT 
    r.Registro_Email AS Email, 
    r.Registro_Usuario AS Nombre, 
    p.Permisos_Rol AS Rol, 
    u.Usuario_imagen AS Foto
FROM Usuarios u
INNER JOIN Registro r ON u.Registro_id = r.Registro_id
INNER JOIN Permisos_Roles p ON u.Permisos_roles_id = p.Permisos_Roles_id;

-- Ver datos de productos en el catalogo (id, Producto, Categoria, Precio, Stock, Imagen(Geneera Problemas))
SELECT 
    p.Prenda_id AS 'ID',
    p.Prenda_nombre AS 'Producto',
    c.Categoria_nombre AS 'Categoría',
    p.Prenda_valor AS 'Precio',
    p.Prenda_stock AS 'Stock Actual',
    img.Imagenes_link AS 'URL Imagen'
FROM Prendas p
INNER JOIN Categoria c ON p.Categoria_id = c.Categoria_id
LEFT JOIN imagenes img ON p.Prenda_id = img.Prenda_id;

-- Para Ver los pedidos a media con los datos de (Id, Usuario, Tipo de prenda, Tela, Medidas que mando, Una Descripcion, Estado)
SELECT 
    dpm.Detalles_PedidoMedida_id AS 'ID Solicitud',
    reg.Registro_Usuario AS 'Cliente',
    dpm.Detalles_TPrenda AS 'Tipo de Prenda',
    dpm.Detalles_Tela AS 'Tela',
    dpm.Detalles_medidas AS 'Medidas Enviadas',
    dpm.Detalles_Descripcion AS 'Descripción del Diseño',
    cp.Cotizacion_Valor AS 'Precio Cotizado ($)',
    cp.ComentarioAdmin AS 'Estado/Comentario Sastre'
FROM DetallesPedidosMedida dpm
INNER JOIN Usuarios u ON dpm.Usuario_id = u.Usuarios_id
INNER JOIN Registro reg ON u.Registro_id = reg.Registro_id
LEFT JOIN CotizacionPedido cp ON dpm.Detalles_PedidoMedida_id = cp.DetallesPedidosMedida_id;

-- Para ver datos de los pedidos Genral (id, Tipo De Pedido, Estado, FEcha de inicio, Metodo de pago, Direccion, total cancelado)
SELECT 
    p.Pedido_id AS 'ID Trabajo',
    p.Pedido_TCompra AS 'Tipo Flujo',
    p.Pedido_Estado AS 'Fase Actual (Card)',
    p.Pedido_FechaInicio AS 'Fecha Inicio',
    cp.ConfirmarPago_MetodoP AS 'Método Pago',
    p.Pedido_Direcccion AS 'Dirección de Entrega',
    -- Si es a medida trae el costo de la cotización, si es catálogo el del detalle del carrito
    COALESCE(cot.Cotizacion_Valor, dc.Detalles_total) AS 'Total Pagado'
FROM Pedidos p
INNER JOIN ConfirmarPago cp ON p.ConfirmarPago_id = cp.ConfirmarPago_id
LEFT JOIN CotizacionPedido cot ON cp.CotizacionPedido_id = cot.CotizacionPedido_Id
LEFT JOIN DetallesCarrito dc ON cp.DetallesCarrito_id = dc.DetallesCarrito_Id;


-- Para verificar el correo o nombre de usuario y la contraseña de un usuario
SELECT r.*, u.Permisos_roles_id, u.Usuario_imagen  
FROM Registro r 
JOIN Usuarios u ON r.Registro_id = u.Registro_id
WHERE (r.Registro_Usuario = 'JavidAdmin' OR r.Registro_Email = '') AND r.Registro_Contraseña = 'Admin123';


-- hacemos la siguiente peticion: solicitamos todos los datos de la tabla prendas, de categoria traemos el nombre
-- de la tabla imagenes el link y de la tabla populares las visitas
SELECT p.*, c.Categoria_nombre, max(i.Imagenes_link) as Imagenes_link, IFNULL(pop.Populares_visitas, 0) as visitas 
				-- Revisamos que las categorias de las prendas tengan una union o semejansa obligatoria en la tabla categorias
                 FROM Prendas p  
                 JOIN Categoria c ON p.Categoria_id = c.Categoria_id 
                 -- left join mostrar la columna del campo x independiente tenga valor o no
                 LEFT JOIN imagenes i ON p.Prenda_id = i.Prenda_id
                 LEFT JOIN Populares pop ON p.Prenda_id = pop.Prenda_id 
                 
                 -- agrupamos los datos de las tablas
                 GROUP BY p.Prenda_id, c.Categoria_nombre, pop.Populares_visitas 
                 -- y las ordenamos segun el numero de visitas de manera decendente
                 ORDER BY visitas DESC;
                 
-- Peticion buscar una prenda en especifico en el catalogo
SELECT p.*, c.Categoria_nombre 
             FROM Prendas p 
             JOIN Categoria c ON p.Categoria_id = c.Categoria_id  
             WHERE p.Prenda_id = 1;	
             
-- Consulta para buscar todas las imagenes
select Imagenes_link from imagenes where Prenda_id = 1;

-- Consultas de pedidos de usuario
-- Primero solicitamos las columnas que vamos a imprimir
SELECT p.Pedido_id, p.Pedido_FechaInicio, p.Pedido_TCompra, p.Pedido_Estado 
			-- Establecemos el punto de partida en la tabla de pedidos
            FROM Pedidos p 
            -- Verifica que los pedidos hayan pasado por la tabla confirmarPago
            JOIN ConfirmarPago cp ON p.ConfirmarPago_id = cp.ConfirmarPago_id
            -- la vifurcacion de los pedidos si es del catalogo en caso de que alguna de las
            -- vifurcaciones esten vacias establece un valor nulo
            LEFT JOIN DetallesCarrito dc ON cp.DetallesCarrito_id = dc.DetallesCarrito_id 
            LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id 
            
            -- O pedidos a medida
            LEFT JOIN CotizacionPedido cot ON cp.CotizacionPedido_id = cot.CotizacionPedido_id
            LEFT JOIN DetallesPedidosMedida dpm ON cot.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id 
            
            -- buscamos al usuario que tiene los pedidos
            WHERE c.Usuarios_id = 8 OR dpm.Usuario_id = 8
            
            -- organizamos los pedidos segun la fecha de manera desendente
            ORDER BY p.Pedido_FechaInicio DESC;

-- Peticiones para revisar los datos de compra:
SELECT 
    IFNULL(SUM(CASE WHEN hp.Historial_Fecha = CURDATE() THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS ganancia_diaria,
    IFNULL(SUM(CASE WHEN YEARWEEK(hp.Historial_Fecha, 1) = YEARWEEK(CURDATE(), 1) THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS ganancia_semanal,
    IFNULL(SUM(CASE WHEN MONTH(hp.Historial_Fecha) = MONTH(CURDATE()) AND YEAR(hp.Historial_Fecha) = YEAR(CURDATE()) THEN COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) ELSE 0 END), 0) AS ganancia_mensual
FROM HistorialPagos hp
JOIN ConfirmarPago cf ON hp.ConfirmarPago_id = cf.ConfirmarPago_id
LEFT JOIN DetallesCarrito dc ON cf.DetallesCarrito_id = dc.DetallesCarrito_Id
LEFT JOIN CotizacionPedido cp ON cf.CotizacionPedido_id = cp.CotizacionPedido_Id;

SELECT 
    hp.HistorialPagos_id AS idPago,
    r.Registro_Usuario AS usuario,
    COALESCE(dc.Detalles_total, cp.Cotizacion_Valor) AS total,
    cf.ConfirmarPago_MetodoP AS metodoPago,
    hp.Historial_Fecha AS fecha,
    cf.ConfirmarPago_TipoPedido AS tipoCompra
FROM HistorialPagos hp
JOIN ConfirmarPago cf ON hp.ConfirmarPago_id = cf.ConfirmarPago_id
LEFT JOIN DetallesCarrito dc ON cf.DetallesCarrito_id = dc.DetallesCarrito_Id
LEFT JOIN Carrito c ON dc.Carrito_id = c.Carrito_id
LEFT JOIN CotizacionPedido cp ON cf.CotizacionPedido_id = cp.CotizacionPedido_Id
LEFT JOIN DetallesPedidosMedida dpm ON cp.DetallesPedidosMedida_id = dpm.Detalles_PedidoMedida_id
JOIN Usuarios u ON u.Usuarios_id = COALESCE(c.Usuarios_id, dpm.Usuario_id)
JOIN Registro r ON u.Registro_id = r.Registro_id
ORDER BY hp.Historial_Fecha DESC;
