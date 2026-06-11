export async function CargarDetallesProd(id) {

    const btnAgregarCarrito = document.querySelector("#btnAgregarCarrito");
    const selectTalla = document.querySelector("#selectTalla");
    const hiddenIdInput = document.querySelector("#prendaIdSeleccionada");
    const txtPrecio = document.querySelector("#detallePrecio");
    // AGREGADO: Capturamos el nuevo input de cantidad
    const inputCantidad = document.querySelector("#inputCantidad"); 

    try {
        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        const producto = await respuesta.json();
        
        console.log("Datos recibidos del Servlet:", producto);
        
        // Asignamos datos globales de la prenda
        document.querySelector("#detalleNombre").textContent = producto.nombre;
        document.querySelector(".product__text").textContent = producto.descripcion;
        
        // 1. Mostrar UNA imagen en concreto
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
        
        // 2. Mostrar TODAS las imágenes en los indicadores
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
            selectTalla.innerHTML = ""; 

            producto.variantes.forEach(variante => {
                const option = document.createElement("option");
                option.value = variante.id; 
                option.textContent = `${variante.talla} (Stock: ${variante.stock})`;
                selectTalla.appendChild(option);
            });

            // Inicializamos la interfaz con los datos de la primera variante
            const primeraVariante = producto.variantes[0];
            hiddenIdInput.value = primeraVariante.id;
            txtPrecio.textContent = `Precio: $${primeraVariante.valor.toFixed(2)}`;
            
            // Ajustamos el valor máximo inicial del input según el stock de la primera variante
            if (inputCantidad) inputCantidad.max = primeraVariante.stock;

            // Evento para cuando el cliente cambie de talla en el select
            selectTalla.addEventListener("change", (e) => {
                const idSeleccionado = parseInt(e.target.value);
                const varianteSeleccionada = producto.variantes.find(v => v.id === idSeleccionado);
                
                if (varianteSeleccionada) {
                    hiddenIdInput.value = varianteSeleccionada.id;
                    txtPrecio.textContent = `Precio: $${varianteSeleccionada.valor.toFixed(2)}`;
                    
                    // CORREGIDO: Si cambia de talla, el máximo permitido del input cambia dinámicamente
                    if (inputCantidad) {
                        inputCantidad.max = varianteSeleccionada.stock;
                        if (parseInt(inputCantidad.value) > varianteSeleccionada.stock) {
                            inputCantidad.value = varianteSeleccionada.stock; // Ajusta si el usuario tenía un número alto
                        }
                    }
                }
            });
        }

        // 4. Asignamos el evento click al botón añadir al carrito
        if (btnAgregarCarrito && producto.variantes) {
            btnAgregarCarrito.addEventListener("click", (e) => {
                
                const idVarianteActual = parseInt(hiddenIdInput.value);
                const varianteSeleccionada = producto.variantes.find(v => v.id === idVarianteActual);

                if (!varianteSeleccionada) {
                    alert("Por favor, selecciona una variante válida.");
                    return;
                }

                // CORREGIDO: Leemos cuántas unidades quiere llevar el usuario realmente
                const cantidadAAgregar = inputCantidad ? parseInt(inputCantidad.value) : 1;

                if (cantidadAAgregar <= 0) {
                    alert("Por favor, ingresa una cantidad válida mayor a 0.");
                    return;
                }

                let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
                const productoActual = carrito.find(item => item.id === idVarianteActual);
    
                if (productoActual) {
                    // VALIDACIÓN MULTI-UNIDAD: Sumamos lo que ya tiene en el carrito + lo que quiere agregar ahora
                    const cantidadTotalProyectada = productoActual.cantidad + cantidadAAgregar;

                    if (cantidadTotalProyectada <= varianteSeleccionada.stock) {
                        productoActual.cantidad = cantidadTotalProyectada;
                    } else {
                        const disponibles = varianteSeleccionada.stock - productoActual.cantidad;
                        alert(`No puedes agregar esa cantidad. Ya tienes ${productoActual.cantidad} en el carrito. Stock máximo disponible restante: ${disponibles}`);
                        return;
                    }
                } else {
                    // VALIDACIÓN NUEVA: Validamos que la cantidad inicial pedida no supere el stock
                    if (cantidadAAgregar <= varianteSeleccionada.stock) {
                        carrito.push({
                            id: varianteSeleccionada.id, 
                            nombre: producto.nombre,
                            precio: varianteSeleccionada.valor,
                            imagen: (producto.listaImagenes && producto.listaImagenes[0]) || "../images/Rectangle 11.png",
                            talla: varianteSeleccionada.talla, 
                            cantidad: cantidadAAgregar // Guardamos la cantidad seleccionada
                        });
                    } else {
                        alert(`Lo sentimos, no hay suficiente stock. Máximo disponible: ${varianteSeleccionada.stock}`);
                        return;
                    }
                }
    
                localStorage.setItem("carritoSastreria", JSON.stringify(carrito));
                alert(`Se agregaron ${cantidadAAgregar} unidad(es) de ${producto.nombre} (${varianteSeleccionada.talla}) al carrito.`);
            });
        }
    } catch (error) {
        console.error("Error al cargar detalles: ", error);
    }
}
