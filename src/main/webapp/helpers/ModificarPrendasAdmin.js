const formulario = document.querySelector("#actualizarProducto");

export function ModificarPrendas(id) {
    // Verificación preventiva de que el formulario exista en el DOM actual
    if (!formulario) return;

    formulario.addEventListener("submit", async (e) => {
        // Evitamos recargas involuntarias de parte del navegador
        e.preventDefault();

        // Se capturan los elementos del DOM
        const nombre = document.querySelector("#nombreProducto").value.trim();
        const Tprecio = document.querySelector("#precioProducto").value.trim(); 
        const talla = document.querySelector("#tallaProducto").value.trim();
        const categoria = document.querySelector("#categoriaProducto").value.trim(); 
        const stockInput = document.querySelector("#stockProducto").value.trim();
        const descripcion = document.querySelector("#descripcion").value.trim();

        console.log(nombre, Tprecio, talla, categoria, stockInput, descripcion);

        const precio = parseFloat(Tprecio);

        // Validar que no hayan campos vacíos
        if (!nombre || !precio || !talla || !categoria || !stockInput || !descripcion) {
            alert("Durante la edición ninguno de los campos puede quedar vacío.");
            return; // Detiene el envío
        }

        if (Tprecio){
            alert("En el campo de valor no deben haber letras/ palabras")
        }

        const stock = parseInt(stockInput, 10);

        // Se valida que el número sea igual o mayor a cero
        if (isNaN(stock) || stock < 0) {
            alert("Por favor, en el campo stock ingrese un número igual o mayor a cero.");
            return;
        }

        // Definición del estado según el stock
        let estado = "activa"; 
        if (stock === 0) {
            estado = "inactiva";
            console.log("El stock del producto actualmente es 0, por lo tanto estará inactivo.");
        }

        // === SOLUCIÓN: Instanciamos formData AQUÍ para que exista antes del bucle ===
        const formData = new FormData();
        formData.append("idPrenda", id);
        formData.append("nombre", nombre);
        formData.append("precio", precio);
        formData.append("talla", talla);
        formData.append("categoria", categoria); 
        formData.append("stock", stock);
        formData.append("estado", estado);
        formData.append("descripcion", descripcion);

        // Recolecciones de imagenes modificadas o actualizadas
        const contenedoresFotos = document.querySelectorAll("#contenedorImgs .img-miniatura-admin");
        let listaImagenesRestantes = [];

        contenedoresFotos.forEach((contenedor, indice) => {
            const imgElement = contenedor.querySelector("img");
            
            if (imgElement) {
                // ¿Es una imagen nueva subida desde el ordenador?
                if (imgElement.hasAttribute("data-nuevo") && imgElement.fileObject) {
                    // Ahora formData sí existe y se puede adjuntar el archivo binario sin problemas
                    formData.append("archivo_imagen_" + indice, imgElement.fileObject);
                } else {
                    // Es una imagen que ya existía en el servidor
                    let rutaSrc = imgElement.getAttribute("src");
                    if (rutaSrc.startsWith("../")) {
                        rutaSrc = rutaSrc.substring(2); 
                    }
                    listaImagenesRestantes.push(rutaSrc);
                }
            }
        });

        

        // Modificado a "imagenesViejas" para acoplarse con la lectura del Servlet
        formData.append("imagenesViejas", JSON.stringify(listaImagenesRestantes));

        // Realizamos la peticion al servlet de ModificarPrendas
        try {
            const respuesta = await fetch("../ModificarPrendaServlet", {
                method: "POST",
                body: formData
            });

            if (!respuesta.ok) throw new Error("Error en la respuesta del servidor");

            const resultado = await respuesta.json(); 

            if (resultado.status === "Exito") {
                alert("¡Prenda modificada con éxito!");
                window.location.href = "InicioAdmin.html"; 
            } else {
                alert("Hubo un error al procesar el cambio: " + resultado.mensaje);
            }

        } catch (error) {
            console.error("Error al enviar la actualización de la prenda:", error);
            alert("No se pudo conectar con el servidor para guardar las modificaciones.");
        }
    });
}