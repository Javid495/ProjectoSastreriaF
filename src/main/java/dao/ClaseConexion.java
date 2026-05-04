package dao; // 1. Indica que este archivo vive en la carpeta 'modelo'

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClaseConexion {
    
    // Variables de configuración
    private static final String NOMBRE_BD = "ModaS";
    private static final String URL = "jdbc:mysql://localhost:3306/" + NOMBRE_BD+ "?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CLAVE = "#Aprendiz2024"; // Tu contraseña de MySQL

    // El método debe ser 'static' para poder usarlo sin crear copias de la clase
    public static Connection getConexion() {
        Connection cn = null;
        try {
            // 2. Cargamos el driver (el que pusiste en WEB-INF/lib)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 3. Intentamos abrir el túnel
            cn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexión establecida con " + NOMBRE_BD);
            
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el Driver JDBC en WEB-INF/lib");
        } catch (SQLException e) {
            System.out.println("Error de SQL: " + e.getMessage());
        }
        return cn;
    }
    
    public static void main(String[] args) {
        // Intentamos obtener la conexión
        Connection testCon = ClaseConexion.getConexion();
        
        if (testCon != null) {
            System.out.println("✅ ¡PRUEBA EXITOSA! El código llega a MySQL.");
            try { testCon.close(); } catch (Exception e) {}
        } else {
            System.out.println("❌ PRUEBA FALLIDA. Revisa la consola para ver el error.");
        }
    }
}