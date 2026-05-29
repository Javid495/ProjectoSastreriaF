import { cargarSelectorCategorias } from "./MostrarCategoriasAdmin.js"

export async function CargarDetallesAdmin(id) {
    try {
        // 1. Cargamos primero el selector (Para que existan las opciones en el DOM)
        await cargarSelectorCategorias();

        // 2. Pedimos los datos del producto al Servlet
        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        if (!respuesta.ok) throw new Error("Error al obtener los detalles del producto");
        
        const producto = await respuesta.json();
        console.log("Datos recibidos del Servlet:", producto);

        // Hidratamos los campos del formulario
        document.querySelector("#nombreProducto").value = producto.nombre || "";
        document.querySelector("#tallaProducto").value = producto.talla || "";
        document.querySelector("#precioProducto").value = producto.valor || 0;
        document.querySelector("#stockProducto").value = producto.stock || 0;
        document.querySelector("#descripcion").value = producto.descripcion || "";
        
        // 3. Selección automática de la categoría (Garantizando consistencia)
        // Usamos 'producto.categoriaId' o el campo exacto en número que mande tu Servlet de detalles
        const selectCat = document.querySelector("#categoriaProducto");
        if (selectCat && producto.categoriaId) {
            selectCat.value = producto.categoriaId;
        } else if (selectCat && producto.categoria) {
            // Alternativa temporal: si el Servlet aún te manda el texto "Camisas", buscamos su ID en las opciones
            const opcionMarcada = Array.from(selectCat.options).find(opt => opt.text === producto.categoria);
            if (opcionMarcada) selectCat.value = opcionMarcada.value;
        }

        // 4. Renderizado seguro de la galería de imágenes
        const galeria = document.querySelector("#contenedorImgs");

        if (galeria) {
            galeria.innerHTML = ""; // ¡NUEVO!: Limpieza preventiva para evitar duplicaciones visuales

            if (producto.listaImagenes && producto.listaImagenes.length > 0) {
                producto.listaImagenes.forEach((urlImagen, indice) => {
                    
                    const contenedorFoto = document.createElement("div");
                    contenedorFoto.classList.add("img-miniatura-admin");
                    contenedorFoto.dataset.indiceImagen = indice; 

                    const img = document.createElement("img");
                    let rutaFinal = (urlImagen.startsWith("http://") || urlImagen.startsWith("https://") || urlImagen.startsWith("data:")) 
                        ? urlImagen 
                        : ".." + urlImagen;
                    
                    img.src = rutaFinal;
                    img.alt = `${producto.nombre} - ${indice + 1}`;

                    const btnEliminarImg = document.createElement("button");
                    btnEliminarImg.type = "button";
                    btnEliminarImg.classList.add("btn-eliminar-foto");
                    btnEliminarImg.innerHTML = "&times;"; 

                    btnEliminarImg.addEventListener("click", () => {
                        if (confirm("¿Estás seguro de que deseas quitar esta imagen de la prenda?")) {
                            contenedorFoto.remove();
                            console.log(`Eliminar la imagen en el índice ${indice} con ruta: ${urlImagen}`);
                        }
                    });

                    contenedorFoto.appendChild(img);
                    contenedorFoto.appendChild(btnEliminarImg);
                    galeria.appendChild(contenedorFoto);
                });
            } else {
                galeria.innerHTML = `
                    <div class="img-miniatura-admin">
                        <img src="../images/Rectangle 11.png" alt="Sin imagen">
                    </div>
                `;
            }
        }
        
    } catch (error) {
        console.error("Error al cargar detalles: ", error);
    }
}