import { cargarSelectorCategorias } from "./MostrarCategoriasAdmin.js";
import { PintarFilaVariante } from "./AñadirModiPrendas.js"; // <-- NUEVO: Importamos el pintor de filas

export async function CargarDetallesAdmin(id) {
    try {
        // 1. Cargamos primero el selector (Para que existan las opciones en el DOM)
        await cargarSelectorCategorias();

        // 2. Pedimos los datos del producto al Servlet (Mantenemos tu endpoint exacto)
        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        if (!respuesta.ok) throw new Error("Error al obtener los detalles del producto");
        
        const producto = await respuesta.json();
        console.log("Datos recibidos del Servlet:", producto);

        // Hidratamos los campos globales del formulario
        document.querySelector("#idPrenda").value = id; // Asegura que el input hidden tenga el ID
        document.querySelector("#nombreProducto").value = producto.nombre || "";
        document.querySelector("#descripcion").value = producto.descripcion || "";
        
        // --- NUEVO: Renderizado del bloque de Variantes (Multitallaje) ---
        const contenedorVariantes = document.querySelector("#contenedorVariantes");
        if (contenedorVariantes) {
            contenedorVariantes.innerHTML = ""; // Limpieza preventiva
            
            if (producto.variantes && producto.variantes.length > 0) {
                producto.variantes.forEach(v => {
                    // Pintamos cada talla pasándole su ID de base de datos correspondiente
                    PintarFilaVariante(contenedorVariantes, v.id, v.talla, v.stock, v.valor);
                });
            } else {
                // Por seguridad, si el objeto no trae variantes, dejamos una fila en blanco limpia
                PintarFilaVariante(contenedorVariantes, null, "", 0, 0);
            }
        }
        
        // 3. Selección automática de la categoría (Garantizando consistencia - Tu lógica intacta)
        const selectCat = document.querySelector("#categoriaProducto");


    if (selectCat) {

        if (producto.categoriaId !== undefined && producto.categoriaId !== null) {
            // Forzamos conversión a String por si el DOM maneja value como texto y el servlet mandó número
            selectCat.value = String(producto.categoriaId);
        } 
        
        else if (producto.categoria) {
         // Limpiamos espacios y pasamos a minúsculas para asegurar el match por texto
            const textoBuscar = producto.categoria.trim().toLowerCase();
        
            const opcionMarcada = Array.from(selectCat.options).find(opt => 
                opt.text.trim().toLowerCase() === textoBuscar
            );
        
            if (opcionMarcada) {
             selectCat.value = opcionMarcada.value;
            }
        }
    
        // Depuración: Si sigue saliendo vacío, vemos en consola qué tiene el select en ese instante
        console.log("Valor asignado al select de categoría:", selectCat.value);
    }

        // 4. Renderizado seguro de la galería de imágenes (TU LOGICA INTACTA)
        const galeria = document.querySelector("#contenedorImgs");

        if (galeria) {
            galeria.innerHTML = ""; // Limpieza preventiva para evitar duplicaciones visuales

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
                    
                    // CLAVE COMPATIBILIDAD: Flag para que el FormData sepa cuáles imágenes no se borraron
                    img.setAttribute("data-vieja", "true"); 

                    const btnEliminarImg = document.createElement("button");
                    btnEliminarImg.type = "button";
                    btnEliminarImg.classList.add("btn-eliminar-foto");
                    btnEliminarImg.innerHTML = "&times;"; 

                    btnEliminarImg.addEventListener("click", () => {
                        if (confirm("¿Estás seguro de que deseas quitar esta imagen de la prenda?")) {
                            contenedorFoto.remove(); // Al remover el nodo completo del DOM, desaparece la imagen y su atributo data-vieja
                            console.log(`Eliminar la imagen en el índice ${indice} con ruta: ${urlImagen}`);
                        }
                    });

                    contenedorFoto.appendChild(img);
                    contenedorFoto.appendChild(btnEliminarImg);
                    galeria.appendChild(contenedorFoto);
                });
            } else {
                // Tu marcador de posición por defecto si la prenda viene sin imágenes
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