export function llamarComponente(selector, rutaArchivo) {
    fetch(rutaArchivo)
        .then(response => {
            if (!response.ok) throw new Error("No se pudo cargar: " + rutaArchivo);
            return response.text();
        })
        .then(data => {
            const elemento = document.querySelector(selector);
            if (elemento) {
                elemento.innerHTML = data;
            }
        })
        .catch(error => console.error("Error:", error));
}

