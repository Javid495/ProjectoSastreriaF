-- creacion de la base de datos de proyecto llamado moda suescun
create database ModaS;

-- uso de la base de datos ModaS
use ModaS;

-- Creacion de tabla de registros usuarios : Solicitar la informacion de nuevos clientes 
create table Registro(
	Registro_id int auto_increment not null primary key,
    Registro_Usuario char(10) not null,
    Registro_Contraseña char(10) not null,
    Registro_Email varchar(50) not null,
    Registro_Telefono char(10) not null
);

-- Tabla de permisos de usuarios : Administar y asignar los permisos segun el tipo de uusario
create table Permisos_Roles(
	Permisos_Roles_id int auto_increment primary key not null,
    Permisos_Rol varchar(50) not null,
    Permisos_Descripcion text null,
    Permisos_asignados text
);

-- Tablas general de usuarios registrados : Alamacena la informacion de todos los usuario registrado: Administrador, cliente 
create table Usuarios (
	Usuarios_id int primary key auto_increment,
    Registro_id int, 
    Permisos_roles_id int,
    
    -- Datos Opcionales que los clientes podran completar una vez ingresen a su perfil
    Usuario_imagen varchar(50) null default "images\Perfil\Ellipse 14.png",
    Usuario_Medidas varchar(50) null,
    foreign key (Registro_id) references Registro(Registro_id),
    foreign key (Permisos_Roles_id) references Permisos_Roles(Permisos_Roles_id)
);

alter table Usuarios modify column Usuario_imagen varchar(50) null default "images\Perfil\Ellipse 14.png";

create table Categoria(
	Categoria_id int auto_increment primary key not null,
    Categoria_nombre varchar(50) not null,
    Categoria_Descripcion text not null
);

-- Tabla Prendas: almacena la informacion registrada de las prendas del catalogo
create table Prendas (
    Prenda_id int auto_increment primary key not null,
    Categoria_id int not null,
    Prenda_nombre varchar(50) not null,
    Prenda_tipo varchar(50) not null,
    Prenda_valor double not null,
    Prenda_talla char(10) not null,
    Prenda_descripcion text not null,
    Prenda_stock int not null default "0",
    Prenda_estado varchar(50) not null,
    foreign key(Categoria_id) references Categoria(Categoria_id)
);


-- Tabla Populares : Alamcena las prendas mas vistas por los usuarios
create table Populares (
	Populares_id int primary key auto_increment,
    Prenda_id int not null,
    
    -- populares_visitas : LLevara la cuenta de cuantas personas han visto un producto
    Populares_visitas char(10) not null,
    foreign key(Prenda_id) references Prendas(Prenda_id)
);


-- Tabla imagenes : la tabla de imagenes almacena las rutas de cada imagen 
create table imagenes (
	Imagenes_id int auto_increment primary key not null,
    Prenda_id int not null,
    
    -- la columna Imagenes_link tendra informacion de tipo texto ya que guardaran las rutas donde se encuentran almacenadas la imagenes
    Imagenes_link text not null,
    foreign key(Prenda_id) references Prendas(Prenda_id)
);


-- Tabla Resenas o "Reseñas" : la tabla reseñas tendran alamacenadas la reseñas de los usuarios con respecto a un producto
create table Resenas(
	Resena_id int auto_increment primary key not null,
	Usuarios_id int not null,
    Prenda_id int not null,
    Resena_descripcion text null,
    Reseña_Imagen text not null,
    foreign key (Usuarios_id) references Usuarios(Usuarios_id),
    foreign key (Prenda_id) references Prendas(Prenda_id)
);


-- Tabla Historial_Recientes : La tabla historial tendra la informacion con respecto a las comprs de los usuarios
create table Historial_Recientes(
	Id_Historial int auto_increment primary key not null,
    Id_Usuarios int not null,
    Id_Prenda int not null,
    
    -- La columna Historial_Fecha Tiene la fecha en la cuall se realizo el pago del pedido
    Historial_Fecha date not null,
    foreign key(Id_Usuarios) references Usuarios(Usuarios_id),
	foreign key(Id_Prenda) references Prendas(Prenda_id)
);

drop table Historial_Recientes;

-- La tabla carrito sera un "guardado" del carrito de compras del usuario
create table Carrito(
	Carrito_id int auto_increment primary key not null,
    Usuarios_id int not null,
    Carrito_fecha date not null,
    foreign key(Usuarios_id) references Usuarios(Usuarios_id)
);

-- La tabla detalles carrito tendra toda la informacion con respecto a la compra actual del cliente
-- Ademas de ir incluido el total de la compra
create table DetallesCarrito(
	DetallesCarrito_Id int auto_increment primary key not null,
    Prendas_id int not null,
    Carrito_id int not null,
    DetallesC_Cantidad char(10) not null,
    Detalles_total decimal(10,2) not null,
    foreign key(Prendas_id) references Prendas(Prenda_id),
    foreign key(Carrito_id) references Carrito(Carrito_id)
);


-- DetallesPedidosMedida es quien recibira los datos con respecto a pedidos que los clientes
-- Quieren que se les realize desde cero
create table DetallesPedidosMedida(
	Detalles_PedidoMedida_id int auto_increment primary key,
    Usuario_id int not null,
    Detalles_medidas varchar(255) not null,
    Detalles_TPrenda varchar(50) not null,
    Detalles_Tela varchar(50) not null,
    Detalles_Descripcion text not null,
    Detalles_Cotizacion decimal(10,2) null,
    Detalles_ComentarioAdmin text null,
    foreign key(Usuario_id) references Usuarios(Usuarios_id)
);

-- CotizacionPedido : es el proceso en el que el administrador decidira si comprometerse a hacer el pedido o no
-- En caso de aceptar se hace la cotizacion del mismo
create table CotizacionPedido(
	CotizacionPedido_Id int auto_increment primary key not null,
    DetallesPedidosMedida_id int not null,
    Solicitud_Pedido varchar(50) not null,
    Cotizacion_FechaLimite date not null,
    foreign key(DetallesPedidosMedida_id) references DetallesPedidosMedida(Detalles_PedidoMedida_id)
);

-- Confirmar pago : esta tabla tendra la informacion de tanto del pago del pedido
-- Como de la direccion de entrega y de conctato del cliente
create table ConfirmarPago(
	ConfirmarPago_id int auto_increment primary key not null,
    CotizacionPedido_id int,
    DetallesCarrito_id int,
    ConfirmarPago_TipoPedido varchar(50) not null,
    ConfirmarPago_MetodoP varchar(50) not null,
    ConfirmarPago_Fecha date not null,
    ConfirmarTelefono char(10) not null,
    foreign key (DetallesCarrito_id) references DetallesCarrito(DetallesCarrito_id),
    foreign key (CotizacionPedido_id) references CotizacionPedido(CotizacionPedido_id)
);

-- La tabla pedidos es quien almacena todos los pedidos que se han realizado
-- como cada pedido tiene un usuario la idea es que cada usuario puedaver solo los pedidos que ha realizado
-- miesntra que el admin tenga libre acceso a ver cada pedido
create table Pedidos(
	Pedido_id int auto_increment primary key not null,
    ConfirmarPago_id int not null,
    Pedido_FechaInicio date not null,
    Pedido_Estado varchar(50) not null,
    Pedido_Direcccion varchar(50) not null,
    Pedido_TCompra varchar(50) not null,
    foreign key(ConfirmarPago_id) references ConfirmarPago(ConfirmarPago_id)
);

-- HistorialPagos es quien tendra la informacion de todos los pedidos ya pagados
-- para que el administrador pueda llevar un control de ganacias del negocio
-- para que el administrador pueda llevar un control de ganacias del negocio
create table HistorialPagos(
	HistorialPagos_id int auto_increment primary key not null,
    ConfirmarPago_id int not null,
    Historial_Fecha date not null,
    foreign key(ConfirmarPago_id) references ConfirmarPago(ConfirmarPago_id)
);
