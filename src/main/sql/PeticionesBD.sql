use ModaS;

SELECT 
    r.Registro_Email AS Email, 
    r.Registro_Usuario AS Nombre, 
    p.Permisos_Rol AS Rol, 
    u.Usuario_imagen AS Foto
FROM Usuarios u
INNER JOIN Registro r ON u.Registro_id = r.Registro_id
INNER JOIN Permisos_Roles p ON u.Permisos_roles_id = p.Permisos_Roles_id;



SELECT r.*, u.Permisos_roles_id, u.Usuario_imagen  
FROM Registro r 
JOIN Usuarios u ON r.Registro_id = u.Registro_id
WHERE (r.Registro_Usuario = 'javid49' OR r.Registro_Email = 'javid49') AND r.Registro_Contraseña = 'hola2027';


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