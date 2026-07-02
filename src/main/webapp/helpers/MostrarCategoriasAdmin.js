// Función para traer las categorías predefinidas de la BD e inyectarlas en el select
export async function cargarSelectorCategorias() {
    const selectCategoria = document.querySelector("#categoriaProducto");
    if (!selectCategoria) return;

    try {
        const respuesta = await fetch("../ObtenerCategorias");
        if (!respuesta.ok) throw new Error("No se pudieron cargar las categorías");

        const categorias = await respuesta.json(); 

        // Limpiamos completamente el contenedor para evitar duplicados
        selectCategoria.innerHTML = "";

        // Creamos la opción por defecto limpia
        const opcionDefecto = document.createElement("option");
        opcionDefecto.value = "";
        opcionDefecto.disabled = true;
        opcionDefecto.selected = true;
        opcionDefecto.textContent = "Seleccione una categoría";
        selectCategoria.appendChild(opcionDefecto);

        // Poblamos las categorías con sus IDs relacionales de la tabla Categoria
        categorias.forEach(cat => {
            const option = document.createElement("option");
            option.value = cat.id;       // Ej: 1
            option.textContent = cat.nombre; // Ej: Camisas
            selectCategoria.appendChild(option);
        });

    } catch (error) {
        console.error("Error al poblar el select de categorías:", error);
        selectCategoria.innerHTML = '<option value="" disabled>Error al cargar categorías</option>';
    }
}