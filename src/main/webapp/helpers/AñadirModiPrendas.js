// helpers/PintarFilaVariante.js
export function PintarFilaVariante(contenedor, id = null, talla = "", stock = "", valor = "") {
    const divRow = document.createElement("div");
    // Al usar 'precios-row' hereda tu gap de 40px y margin-bottom de 35px automáticamente
    divRow.classList.add("precios-row", "fila-variante-item");
    
    if (id) {
        divRow.setAttribute("data-id-variante", id);
    }

    // Encajamos los inputs dentro de tus 'precio-group' nativos
    divRow.innerHTML = `
        <div class="precio-group">
            <input type="text" class="input-precio var-talla" value="${talla}" placeholder="Ej: M" required>
        </div>
        <div class="precio-group">
            <input type="number" class="input-precio var-stock" value="${stock}" placeholder="0" required min="0">
        </div>
        <div class="precio-group">
            <input type="number" step="0.01" class="input-precio var-precio" value="${valor}" placeholder="0.00" required min="0">
        </div>
        <button type="button" class="btn-eliminar-variante" title="Eliminar variante" 
                style="background-color: rgba(255, 0, 0, 0.1); color: red; border: none; border-radius: 50%; width: 28px; height: 28px; font-size: 18px; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; align-self: center; transition: background 0.2s;">
            ×
        </button>
    `;

    // Efecto hover simple para el botón eliminar variante
    const btnEliminar = divRow.querySelector(".btn-eliminar-variante");
    btnEliminar.addEventListener("mouseover", () => btnEliminar.style.backgroundColor = "rgba(255, 0, 0, 0.2)");
    btnEliminar.addEventListener("mouseout", () => btnEliminar.style.backgroundColor = "rgba(255, 0, 0, 0.1)");

    // Evento de remoción del nodo
    btnEliminar.addEventListener("click", () => {
        divRow.remove();
    });

    contenedor.appendChild(divRow);
}