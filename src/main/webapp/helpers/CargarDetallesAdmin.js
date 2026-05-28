
export async function CargarDetallesAdmin(id) {

    try{

        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        
        const producto = await respuesta.json();
        console.log("Datos recibidos del Servlet:", producto);

        console.log(producto.valor);
        

        document.querySelector("#nombreProducto").value = producto.nombre;
        document.querySelector("#tallaProducto").value = producto.talla;
        document.querySelector("#precioProducto").value = producto.valor;
        document.querySelector("#categoriaProducto").value = producto.categoria;
        document.querySelector("#stockProducto").value = producto.stock;
        document.querySelector("#descripcion").value = producto.descripcion
        
        console.log("Encuentra la lista");

        // Capturamos el contenedor de la galería del administrador
        const galeria = document.querySelector("#contenedorImgs");

        if (galeria) {

        if (producto.listaImagenes && producto.listaImagenes.length > 0) {
        producto.listaImagenes.forEach((urlImagen, indice) => {
            
            // Crearmos el contenedor individual para la foto y su acción
            const contenedorFoto = document.createElement("div");
            contenedorFoto.classList.add("img-miniatura-admin");
            
            // Guardamos el índice o la ruta en el dataset para saber cuál borrar después
            contenedorFoto.dataset.indiceImagen = indice; 

            // 2. Creamos el elemento de la imagen
            const img = document.createElement("img");
            let rutaFinal = (urlImagen.startsWith("http://") || urlImagen.startsWith("https://") || urlImagen.startsWith("data:")) 
                ? urlImagen 
                : ".." + urlImagen;
            
            img.src = rutaFinal;
            img.alt = `${producto.nombre} - ${indice + 1}`;

            // 3. Creamos el botón/ícono de eliminación
            const btnEliminarImg = document.createElement("button");
            btnEliminarImg.type = "button";
            btnEliminarImg.classList.add("btn-eliminar-foto");
            
            // Puedes usar una "X" de texto, un emoji (❌), o una clase de FontAwesome si usas íconos
            btnEliminarImg.innerHTML = "&times;"; // Esto pinta una 'X' elegante de cierre

            // 4. Evento para eliminar la imagen al hacer clic
            btnEliminarImg.addEventListener("click", () => {
                if (confirm("¿Estás seguro de que deseas quitar esta imagen de la prenda?")) {
                    // Acción inmediata en el Front: desvanecer o quitar de la vista
                    contenedorFoto.remove();
                    
                    // Lógica para el Backend:
                    console.log(`Eliminar la imagen en el índice ${indice} con ruta: ${urlImagen}`);
                    // Aquí podrás hacer un fetch a un servlet encargado de borrar esa relación en la tabla 'imagenes'
                }
            });

            // 5. Armamos el rompecabezas: metemos la imagen y el botón en el contenedor, y este a la galería
            contenedorFoto.appendChild(img);
            contenedorFoto.appendChild(btnEliminarImg);
            galeria.appendChild(contenedorFoto);
        });
        } 
    
        else {
        // Si el producto no tiene fotos, mostramos la de por defecto
        galeria.innerHTML = `
            <div class="img-miniatura-admin">
                <img src="../images/Rectangle 11.png" alt="Sin imagen">
            </div>
        `;
        }
        }
        
    }
    catch (error){
        console.error("Error al cargar detalles: ", error)
    }

} 