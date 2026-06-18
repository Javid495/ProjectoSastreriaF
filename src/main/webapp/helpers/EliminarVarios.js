export async function procesarEliminacionMasiva(desactivarModoUI) {
    // 1. Buscamos todos los checkboxes seleccionados (usando tu clase .custom-checkbox)
    const checkboxesSeleccionados = document.querySelectorAll(".custom-checkbox:checked");

    if (checkboxesSeleccionados.length === 0) {
        alert("Por favor, selecciona al menos una prenda para eliminar en grupo.");
        return false; // Retornamos false porque no se procesó nada
    }

    const listaIds = [];
    const listaNombres = [];

    // 2. Recorremos los elementos seleccionados escalando hasta el <article>
    checkboxesSeleccionados.forEach(chk => {
        const articlePrenda = chk.closest("article"); 
        
        if (articlePrenda) {
            const id = articlePrenda.getAttribute("data-id");
            const etiquetaNombre = articlePrenda.querySelector("p") || articlePrenda.querySelector(".nombre-prenda");
            const nombre = etiquetaNombre ? etiquetaNombre.textContent.trim() : "Producto sin nombre";

            if (id) {
                listaIds.push(parseInt(id));
                listaNombres.push(nombre);
            }
        }
    });

    // 3. Construcción del cuadro de diálogo (CORREGIDO con Semántica de Borrado Lógico)
    let mensajeProductos = listaNombres.map(nombre => `• ${nombre}`).join("\n");
    const mensajeConfirmacion = `¿Estás completamente seguro de deshabilitar las siguientes ${listaIds.length} prendas?\n\n${mensajeProductos}\n\n⚠️ Los productos se ocultarán de los catálogos clientes, pero se preservará su historial de ventas.`;

    // 🌟 CONTROL DE FLUJO: Si cancela el diálogo, devolvemos false de inmediato
    if (!confirm(mensajeConfirmacion)) {
        return false; 
    }

    try {
        console.log("✈️ Despachando lote de IDs al Servlet:", listaIds);

        const respuesta = await fetch("../EliminarVariasPrendas", {
            method: "POST", 
            headers: {
                "Content-Type": "application/json; charset=UTF-8"
            },
            body: JSON.stringify({ ids: listaIds })
        });

        const resultado = await respuesta.json();

        if (resultado.status === "Exito") {
            alert("¡Lote de prendas deshabilitado con éxito!");
            
            // Remover las tarjetas visualmente del DOM de inmediato
            checkboxesSeleccionados.forEach(chk => {
                const articlePrenda = chk.closest("article");
                if (articlePrenda) {
                    const cardCompleta = articlePrenda.closest(".card-inventario") || articlePrenda;
                    cardCompleta.remove();
                }
            });

            // Regresamos la interfaz al modo normal
            desactivarModoUI();
            
            return true; // 🌟 ÉXITO ABSOLUTO
            
        } else {
            alert("Error al eliminar el lote: " + resultado.mensaje);
            return false;
        }

    } catch (error) {
        console.error("Error en la petición masiva:", error);
        alert("No se pudo establecer conexión con el servidor para la eliminación grupal.");
        return false;
    }
}


