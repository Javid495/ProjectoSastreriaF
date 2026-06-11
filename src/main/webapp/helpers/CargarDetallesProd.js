export async function CargarDetallesProd(id) {

    const btnAgregarCarrito = document.querySelector("#btnAgregarCarrito");
    const selectTalla = document.querySelector("#selectTalla");
    const hiddenIdInput = document.querySelector("#prendaIdSeleccionada");
    const txtPrecio = document.querySelector("#detallePrecio");

    try {
        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        const producto = await respuesta.json();
        
        console.log("Datos recibidos del Servlet:", producto);
        
        // Asignamos datos globales de la prenda
        document.querySelector("#detalleNombre").textContent = producto.nombre;
        document.querySelector(".product__text").textContent = producto.descripcion;
        
        // 1. Mostrar UNA imagen en concreto (la primera como principal)
        const imgPrincipal = document.querySelector(".product__image img");
        if (imgPrincipal && producto.listaImagenes && producto.listaImagenes.length > 0) {
            const primeraImagen = producto.listaImagenes[0];
            if (primeraImagen.startsWith("http://") || primeraImagen.startsWith("https://") || primeraImagen.startsWith("data:")) {
                imgPrincipal.src = primeraImagen;
            } else {
                imgPrincipal.src = ".." + primeraImagen;
            }
            imgPrincipal.alt = producto.nombre;
        }
        
        // 2. Mostrar TODAS las imágenes en los indicadores (Tu lógica original intacta)
        const contenedorDots = document.querySelector(".product__dots");
        contenedorDots.innerHTML = ""; 
        if (producto.listaImagenes) {
            producto.listaImagenes.forEach((urlImagen, indice) => {
                const dot = document.createElement("span");
                dot.classList.add("product__dot");
                if (indice === 0) dot.classList.add("product__dot--active"); 
        
                let rutaFinal = "";
                if (urlImagen.startsWith("http://") || urlImagen.startsWith("https://") || urlImagen.startsWith("data:")) {
                    rutaFinal = urlImagen;
                } else {
                    rutaFinal = ".." + urlImagen;
                }
        
                dot.dataset.ruta = rutaFinal; 
                contenedorDots.appendChild(dot);
            });
        }

        // 3. Control y renderizado dinámico del Selector de Tallas
        if (selectTalla && producto.variantes && producto.variantes.length > 0) {
            selectTalla.innerHTML = ""; // Limpiamos opciones estáticas

            producto.variantes.forEach(variante => {
                const option = document.createElement("option");
                option.value = variante.id; // Guardamos el ID real de esta variante específica
                option.textContent = `${variante.talla} (Stock: ${variante.stock})`;
                selectTalla.appendChild(option);
            });

            // Inicializamos la interfaz con los datos de la primera variante de la lista
            const primeraVariante = producto.variantes[0];
            hiddenIdInput.value = primeraVariante.id;
            txtPrecio.textContent = `Precio: $${primeraVariante.valor.toFixed(2)}`;

            // Evento para cuando el cliente cambie de talla en el select
            selectTalla.addEventListener("change", (e) => {
                const idSeleccionado = parseInt(e.target.value);
                // Buscamos la variante correspondiente en los datos locales
                const varianteSeleccionada = producto.variantes.find(v => v.id === idSeleccionado);
                
                if (varianteSeleccionada) {
                    hiddenIdInput.value = varianteSeleccionada.id;
                    txtPrecio.textContent = `Precio: $${varianteSeleccionada.valor.toFixed(2)}`;
                }
            });
        }

        // 4. Asignamos el evento click al botón añadir al carrito
        if (btnAgregarCarrito && producto.variantes) {
            btnAgregarCarrito.addEventListener("click", (e) => {
                
                // Obtenemos el ID de la variante que está actualmente seleccionada
                const idVarianteActual = parseInt(hiddenIdInput.value);
                const varianteSeleccionada = producto.variantes.find(v => v.id === idVarianteActual);

                if (!varianteSeleccionada) {
                    alert("Por favor, selecciona una variante válida.");
                    return;
                }

                let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
                // Buscamos en el carrito si ya existe esta prenda en esta talla específica
                const productoActual = carrito.find(item => item.id === idVarianteActual);
    
                if (productoActual) {
                    // Validamos que el cliente no intente pedir más del stock real en base de datos
                    if (productoActual.cantidad < varianteSeleccionada.stock) {
                        productoActual.cantidad += 1;
                    } else {
                        alert(`Lo sentimos, no puedes agregar más unidades. Stock máximo para talla ${varianteSeleccionada.talla}: ${varianteSeleccionada.stock}`);
                        return;
                    }
                } else {
                    // Si el producto/talla es nuevo en el carrito, lo registramos
                    carrito.push({
                        id: varianteSeleccionada.id, // El ID de la variante específica
                        nombre: producto.nombre,
                        precio: varianteSeleccionada.valor,
                        imagen: (producto.listaImagenes && producto.listaImagenes[0]) || "../images/Rectangle 11.png",
                        talla: varianteSeleccionada.talla, // Corregido el typo "producto.tall"
                        cantidad: 1
                    });
                }
    
                localStorage.setItem("carritoSastreria", JSON.stringify(carrito));
                alert(`El producto ${producto.nombre} (${varianteSeleccionada.talla}) se agregó correctamente al carrito.`);
            });
        }
    } catch (error) {
        console.error("Error al cargar detalles: ", error);
    }
}
