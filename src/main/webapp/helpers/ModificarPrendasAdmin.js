const formulario = document.querySelector("#actualizarProducto");

export function ModificarPrendas(id) {
    // Verificación preventiva de que el formulario exista en el DOM actual
    if (!formulario) return;

    formulario.addEventListener("submit", async (e) => {
        // Evitamos recargas involuntarias de parte del navegador
        e.preventDefault();

        // 1. Capturamos los elementos GLOBALES del DOM
        const nombre = document.querySelector("#nombreProducto").value.trim();
        const categoria = document.querySelector("#categoriaProducto").value.trim(); 
        const descripcion = document.querySelector("#descripcion").value.trim();

        // 2. RECOLECCIÓN Y VALIDACIÓN DINÁMICA DE VARIANTES (TALLAS)
        const filasVariantes = document.querySelectorAll(".fila-variante-item");
        let listaVariantesEnviar = [];
        let totalStockProducto = 0;
        let tieneCamposVacios = false;
        let tieneErrorPrecio = false;
        let tieneErrorStock = false;

        // Validar que al menos exista una fila de talla
        if (filasVariantes.length === 0) {
            alert("Debes definir al menos una variante de talla para el producto.");
            return;
        }

        filasVariantes.forEach(fila => {
            const idVariante = fila.getAttribute("data-id-variante");
            const tallaInput = fila.querySelector(".var-talla").value.trim();
            const stockInput = fila.querySelector(".var-stock").value.trim();
            const precioInput = fila.querySelector(".var-precio").value.trim();

            // Validación: Campos vacíos en la fila
            if (!tallaInput || !stockInput || !precioInput) {
                tieneCamposVacios = true;
                return;
            }

            const precio = parseFloat(precioInput);
            const stock = parseInt(stockInput, 10);

            // Validación: Si el precio no es un número válido
            if (isNaN(precio) || precio <= 0) {
                tieneErrorPrecio = true;
            }

            // Validación: Si el stock es negativo o inválido
            if (isNaN(stock) || stock < 0) {
                tieneErrorStock = true;
            }

            // Si pasa los filtros iniciales, acumulamos el stock global
            totalStockProducto += isNaN(stock) ? 0 : stock;

            // Construimos el objeto de la variante
            let objetoVariante = {
                talla: tallaInput,
                stock: stock,
                valor: precio
            };

            // Si la variante ya existía en la BD, conservamos su ID para que el DAO la actualice
            if (idVariante) {
                objetoVariante.id = parseInt(idVariante, 10);
            }

            listaVariantesEnviar.push(objetoVariante);
        });

        // --- Lanzador de tus Alertas Nativas ---
        if (!nombre || !categoria || !descripcion || tieneCamposVacios) {
            alert("Durante la edición ninguno de los campos puede quedar vacío.");
            return; 
        }

        if (tieneErrorPrecio) {
            alert("En el campo de valor no deben haber letras/palabras y debe ser mayor a 0.");
            return;
        }

        if (tieneErrorStock) {
            alert("Por favor, en el campo stock ingrese un número igual o mayor a cero.");
            return;
        }

        // Definición del estado global según el stock acumulado de todas las tallas
        let estado = "activa"; 
        if (totalStockProducto === 0) {
            estado = "inactiva";
            console.log("El stock total de todas las variantes actualmente es 0, por lo tanto estará inactivo.");
        }

        // 3. INSTANCIAMOS EL FORMDATA
        const formData = new FormData();
        formData.append("idPrenda", id);
        formData.append("nombre", nombre);
        formData.append("categoria", categoria); 
        formData.append("estado", estado);
        formData.append("descripcion", descripcion);
        
        // Adjuntamos el JSON string de las variantes que procesará tu Servlet
        formData.append("variantes", JSON.stringify(listaVariantesEnviar));

        // 4. RECOLECCIÓN DE IMÁGENES (Tu lógica original intacta y pulida)
        const contenedoresFotos = document.querySelectorAll("#contenedorImgs .img-miniatura-admin");
        let listaImagenesRestantes = [];

        contenedoresFotos.forEach((contenedor, indice) => {
            const imgElement = contenedor.querySelector("img");
            
            if (imgElement) {
                // ¿Es una imagen nueva subida desde el ordenador?
                if (imgElement.hasAttribute("data-nuevo") && imgElement.fileObject) {
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

        // Enviamos las imágenes viejas que sobrevivieron al borrado del usuario
        formData.append("imagenesViejas", JSON.stringify(listaImagenesRestantes));

        // 5. REALIZAMOS LA PETICIÓN AL SERVLET
        try {
            const respuesta = await fetch("../ModificarPrendaServlet", {
                method: "POST",
                body: formData
            });

            if (!respuesta.ok) throw new Error("Error en la respuesta del servidor");

            const resultado = await respuesta.json(); 

            if (resultado.status === "Exito") {
                alert("¡Prenda modificada con éxito!");
                window.location.href = "InicioAdmin.html"; // Tu redirección nativa
            } else {
                alert("Hubo un error al procesar el cambio: " + resultado.mensaje);
            }

        } catch (error) {
            console.error("Error al enviar la actualización de la prenda:", error);
            alert("No se pudo conectar con el servidor para guardar las modificaciones.");
        }
    });
}