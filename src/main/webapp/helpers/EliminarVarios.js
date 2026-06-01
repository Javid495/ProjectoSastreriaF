

export async function procesarEliminacionMasiva(desactivarModoUI) {
    // 1. Buscamos todos los checkboxes seleccionados (usando tu clase .custom-checkbox)
    const checkboxesSeleccionados = document.querySelectorAll(".custom-checkbox:checked");

    if (checkboxesSeleccionados.length === 0) {
        alert("Por favor, selecciona al menos una prenda para eliminar en grupo.");
        return;
    }

    const listaIds = [];
    const listaNombres = [];

    // 2. Recorremos los elementos seleccionados escalando hasta el <article>
    checkboxesSeleccionados.forEach(chk => {
        const articlePrenda = chk.closest("article"); 
        
        if (articlePrenda) {
            const id = articlePrenda.getAttribute("data-id");
            // Buscamos el párrafo o etiqueta que tiene el nombre dentro de tu diseño de card
            const etiquetaNombre = articlePrenda.querySelector("p") || articlePrenda.querySelector(".nombre-prenda");
            const nombre = etiquetaNombre ? etiquetaNombre.textContent.trim() : "Producto sin nombre";

            if (id) {
                listaIds.push(parseInt(id));
                listaNombres.push(nombre);
            }
        }
    });

    // 3. Construcción del cuadro de diálogo de confirmación
    let mensajeProductos = listaNombres.map(nombre => `• ${nombre}`).join("\n");
    const mensajeConfirmacion = `¿Estás completamente seguro de eliminar las siguientes ${listaIds.length} prendas?\n\n${mensajeProductos}\n\n⚠️ Esta acción borrará permanentemente los registros y sus fotos.`;

    if (confirm(mensajeConfirmacion)) {
        try {
            console.log("✈️ Despachando lote de IDs al Servlet:", listaIds);

            const respuesta = await fetch("../EliminarVariasPrendas", {
                method: "POST", // Método POST para procesar el JSON en el lote masivo
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ ids: listaIds })
            });

            const resultado = await respuesta.json();

            if (resultado.status === "Exito") {
                alert("¡Lote de prendas eliminado con éxito!");
                
                // Remover las tarjetas visualmente del DOM de inmediato
                checkboxesSeleccionados.forEach(chk => {
                    const articlePrenda = chk.closest("article");
                    // Ajustamos para remover el contenedor padre real de la card (.card-inventario)
                    if (articlePrenda) {
                        const cardCompleta = articlePrenda.closest(".card-inventario") || articlePrenda;
                        cardCompleta.remove();
                    }
                });

                // Regresamos la interfaz al modo normal pasándole la función que limpia la UI
                desactivarModoUI();
                
            } else {
                alert("Error al eliminar el lote: " + resultado.mensaje);
            }

        } catch (error) {
            console.error("Error en la petición masiva:", error);
            alert("No se pudo establecer conexión con el servidor para la eliminación grupal.");
        }
    }
}


