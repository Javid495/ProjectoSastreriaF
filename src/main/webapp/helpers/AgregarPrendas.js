// ../helpers/AgregarPrendas.js

export function RegistrarPrendas() {
    // Buscamos el formulario usando el ID exacto de tu HTML: id="registrarProducto"
    const formulario = document.getElementById("registrarProducto");
    
    if (!formulario) {
        console.warn("No se encontró el formulario #registrarProducto en el DOM.");
        return;
    }

    formulario.addEventListener("submit", async (e) => {
        e.preventDefault(); // Detenemos la recarga de página por defecto

        // 1. CAPTURA DE VALORES BASE (Campos únicos del producto)
        const nombre = document.getElementById("nombreProducto").value.trim();
        const categoria = document.getElementById("categoriaProducto").value;
        const descripcion = document.getElementById("descripcion").value.trim();

        // Validación previa de los datos de cabecera obligatorios
        if (!nombre || !categoria) {
            alert("Por favor, completa los campos obligatorios principales (Nombre y Categoría).");
            return;
        }

        // 2. RECOLECCIÓN DINÁMICA DE TODAS LAS VARIANTES EN PANTALLA
        const filasVariantes = document.querySelectorAll(".fila-variante");
        const loteVariantes = [];
        let validacionPreciosStock = true;

        filasVariantes.forEach(fila => {
            // Buscamos por clase dentro de la fila específica actual
            const talla = fila.querySelector(".input-talla").value.trim();
            const precioRaw = fila.querySelector(".input-precio").value.trim();
            const stockRaw = fila.querySelector(".input-stock").value.trim();

            // Solo procesamos la fila si el administrador escribió algo en la Talla
            if (talla) {
                const precio = parseFloat(precioRaw) || 0.0;
                const stock = parseInt(stockRaw, 10) || 0;

                // Validación de negocio intermedia
                if (precio <= 0) {
                    validacionPreciosStock = false;
                }

                loteVariantes.push({
                    talla: talla,
                    stock: stock,
                    valor: precio // <-- Clave "valor" idéntica a lo esperado por variante.get("valor") en Java
                });
            }
        });

        // Validaciones de las variantes recolectadas
        if (loteVariantes.length === 0) {
            alert("Debe ingresar al menos una variante con su Talla, Precio y Stock.");
            return;
        }

        if (!validacionPreciosStock) {
            alert("Por favor, asegúrate de que todas las tallas ingresadas tengan un precio mayor a 0.");
            return;
        }

        // 3. CONSTRUCCIÓN DEL OBJETO MULTIPART (FormData)
        const formData = new FormData();
        
        formData.append("nombreProducto", nombre);
        formData.append("categoria", categoria);
        formData.append("descripcion", descripcion);
        formData.append("tipoProducto", "Prenda"); 
        formData.append("estado", "activa");        

        // Empaquetamos el array completo de mapas en un único String JSON
        formData.append("variantes", JSON.stringify(loteVariantes));

        // 4. RECOLECCIÓN DE IMÁGENES DESDE EL DOM (Mantiene tu lógica intacta)
        const imagenesEnPantalla = document.querySelectorAll(".img-miniatura-admin img");
        
        imagenesEnPantalla.forEach((img, indice) => {
            if (img.fileObject) {
                formData.append(`archivo_imagen_${indice}`, img.fileObject);
            }
        });

        // 5. ENVÍO ASÍNCRONO AL SERVLET
        try {
            const respuesta = await fetch("../RegistrarPrendaServlet", {
                method: "POST",
                body: formData 
            });

            const resultado = await respuesta.json();

            if (respuesta.ok && resultado.status === "Exito") {
                alert("🎉 " + resultado.mensaje);
                
                // --- LIMPIEZA INTEGRAL DEL FORMULARIO ---
                formulario.reset();
                
                // Removemos las miniaturas de imágenes de la pantalla
                document.querySelectorAll(".img-miniatura-admin").forEach(div => div.remove());
                
                // Limpieza de filas clonadas: dejamos solo la primera fila limpia y borramos las demás
                const filas = document.querySelectorAll(".fila-variante");
                filas.forEach((fila, indice) => {
                    if (indice > 0) {
                        fila.remove();
                    }
                });
                
            } else {
                alert("Error en el servidor: " + (resultado.mensaje || "No se pudo registrar la prenda."));
            }

        } catch (error) {
            console.error("Error detectado en fetch:", error);
            alert("Ocurrió un fallo en la comunicación con el servidor.");
        }
    });
}