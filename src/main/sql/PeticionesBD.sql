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