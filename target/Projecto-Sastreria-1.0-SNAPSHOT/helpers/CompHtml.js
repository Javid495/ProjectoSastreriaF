export async function llamarComponente(selector, rutaArchivo) {
    try {
        // El 'await' aquí hace que la función espere la respuesta del servidor
        const response = await fetch(rutaArchivo);
        
        if (!response.ok) {
            throw new Error("No se pudo cargar: " + rutaArchivo);
        }

        // Esperamos a que el cuerpo de la respuesta se convierta a texto
        const data = await response.text();
        
        const elemento = document.querySelector(selector);
        if (elemento) {
            elemento.innerHTML = data;
            // Aquí el componente ya existe físicamente en el HTML
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

