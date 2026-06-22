USE ModaSv1;

-- Usuarios y roles
INSERT INTO Registro(Registro_id, Registro_Usuario, Registro_Contraseña, Registro_Email, Registro_Telefono) VALUES 
(1, "JavidAdmin", "Admin123", "juandavidcaceres@gmail.com", "3155746387"),
(2, "JavidUser", "Hola123", "juandavid@gmail.com", "3155746387");

INSERT INTO Permisos_Roles(Permisos_Roles_id, Permisos_Rol, Permisos_Descripcion, Permisos_asignados) VALUES
(1, "Cliente", "Permisos para acceder tanto a las funciones de perfil y carrito de compra", "cliente"),
(2, "Administrador", "permisos para acceder a la interfaz principal de administrador", "admin");

INSERT INTO Usuarios (Registro_id, Permisos_roles_id, Usuario_imagen) VALUES 
(1, 2, "images/Perfil/Ellipse 14.png"), 
(2, 1, "images/Perfil/Ellipse 14.png");

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
(1, "/images/Rectangle38.png"), -- 🔥 Recomendado: Rutas relativas sin / inicial si manejas urlBase en JS
(2, "/images/Rectangle38.png"), 
(3, "/images/Rectangle35.png"),
(4, "/images/Rectangle38.png"), 
(5, "/images/Rectangle35.png");



