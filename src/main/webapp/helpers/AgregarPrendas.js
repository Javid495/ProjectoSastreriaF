// helpers/RegistrarPrendasAdmin.js

export function RegistrarPrendas() {
    const formulario = document.querySelector("#registrarProducto");

    // Verificación preventiva de que el formulario exista en el DOM actual
    if (!formulario) return;

    formulario.addEventListener("submit", async (e) => {
        // Evitamos recargas involuntarias del navegador
        e.preventDefault();

        // Captura de los elementos del DOM usando los ID del nuevo diseño
        const nombre = document.querySelector("#nombreProducto").value.trim();
        const talla = document.querySelector("#tallaProducto").value.trim();
        const Tprecio = document.querySelector("#precioProducto").value.trim(); 
        const stockInput = document.querySelector("#stockProducto").value.trim();
        const categoria = document.querySelector("#categoriaProducto").value.trim(); 
        const descripcion = document.querySelector("#descripcion").value.trim();

        const precio = parseFloat(Tprecio);

        // 1. Validar que no haya campos vacíos
        if (!nombre || !talla || isNaN(precio) || !stockInput || !categoria || !descripcion) {
            alert("Por favor, complete todos los campos del formulario antes de registrar la prenda.");
            return; // Detiene el envío
        }

        const stock = parseInt(stockInput, 10);

        // 2. Validar que el stock sea un número coherente
        if (isNaN(stock) || stock < 0) {
            alert("Por favor, en el campo stock ingrese un número igual o mayor a cero.");
            return;
        }

        // 3. Construcción del FormData para el envío Multipart (Texto + Binarios)
        const formData = new FormData();
        formData.append("nombreProducto", nombre);
        formData.append("talla", talla);
        formData.append("precio", precio);
        formData.append("stock", stock);
        formData.append("categoria", categoria); // Envía el ID numérico de la FK
        formData.append("descripcion", descripcion);

        // 4. Recolección de las imágenes subidas a la miniatura

        const contenedoresFotos = document.querySelectorAll("#contenedorImgs .img-miniatura-admin");
        
        contenedoresFotos.forEach((contenedor, indice) => {
            const imgElement = contenedor.querySelector("img");
            
            // Leemos el archivo binario puro que guardamos previamente en memoria
            if (imgElement && imgElement.fileObject) {
                formData.append("archivo_imagen_" + indice, imgElement.fileObject);
                console.log(`Adjuntado con éxito al FormData: archivo_imagen_${indice}`);
            }
        });


        // 5. Envío asíncrono al nuevo Servlet de Registro
        try {
            const respuesta = await fetch("../RegistrarPrendaServlet", {
                method: "POST",
                body: formData
            });

            if (!respuesta.ok) throw new Error("Error en la respuesta del servidor");

            const resultado = await respuesta.json(); 

            if (resultado.status === "Exito") {
                alert("¡Prenda registrada y guardada con éxito!");
                window.location.href = "InicioAdmin.html"; // Redirección al catálogo principal
            } else {
                alert("Hubo un error al registrar el producto: " + resultado.mensaje);
            }

        } catch (error) {
            console.error("Error al enviar el registro de la prenda:", error);
            alert("No se pudo conectar con el servidor para guardar la nueva prenda.");
        }
    });
}