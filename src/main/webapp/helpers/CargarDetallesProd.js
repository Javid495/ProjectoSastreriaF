export async function CargarDetallesProd(id) {

    const btnAgregarCarrito = document.querySelector("#btnAgregarCarrito");
    const selectTalla = document.querySelector("#selectTalla");
    const hiddenIdInput = document.querySelector("#prendaIdSeleccionada");
    const txtPrecio = document.querySelector("#detallePrecio");
    const inputCantidad = document.querySelector("#inputCantidad"); 
    const txtStock = document.querySelector("#detalleStock");
    
    // CAPTURAS DE LA GALERÍA
    const imgPrincipal = document.querySelector(".product__image img");
    const contenedorDots = document.querySelector(".product__dots");
    const btnIzquierda = document.querySelector(".product__arrow--left");
    const btnDerecha = document.querySelector(".product__arrow--right");

    try {
        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        const producto = await respuesta.json();
        
        console.log("Datos recibidos del Servlet:", producto);
        
        // Asignamos datos globales de la prenda
        document.querySelector("#detalleNombre").textContent = producto.nombre;
        document.querySelector(".product__text").textContent = producto.descripcion;
        
        // =========================================================================
        // 🔄 NUEVA LÓGICA DE LA GALERÍA INTERACTIVA (CARRUSEL)
        // =========================================================================
        let indiceActual = 0;
        const imagenes = producto.listaImagenes || [];

        // Función reutilizable para refrescar la imagen y los dots activos
        const cambiarImagen = (nuevoIndice) => {
            if (imagenes.length === 0) return;

            // Bucle infinito: si sobrepasa el máximo vuelve a 0, si baja de 0 va al último
            if (nuevoIndice >= imagenes.length) indiceActual = 0;
            else if (nuevoIndice < 0) indiceActual = imagenes.length - 1;
            else indiceActual = nuevoIndice;

            const urlImagen = imagenes[indiceActual];
            let rutaFinal = "";
            if (urlImagen.startsWith("http://") || urlImagen.startsWith("https://") || urlImagen.startsWith("data:")) {
                rutaFinal = urlImagen;
            } else {
                rutaFinal = ".." + urlImagen;
            }

            if (imgPrincipal) {
                imgPrincipal.src = rutaFinal;
                imgPrincipal.alt = `${producto.nombre} - Imagen ${indiceActual + 1}`;
            }

            // Actualizar visualmente qué puntito está activo
            const todosLosDots = contenedorDots.querySelectorAll(".product__dot");
            todosLosDots.forEach((dot, idx) => {
                if (idx === indiceActual) {
                    dot.classList.add("product__dot--active");
                } else {
                    dot.classList.remove("product__dot--active");
                }
            });
        };

        // 1. Renderizar la primera imagen de forma inicial
        if (imagenes.length > 0) {
            cambiarImagen(0);
        } else {
            if (imgPrincipal) imgPrincipal.src = "../images/Rectangle 11.png"; // Imagen por defecto por si no hay
        }

        // 2. Crear dinámicamente los indicadores (Dots) y asignarles su clic
        contenedorDots.innerHTML = ""; 
        imagenes.forEach((urlImagen, indice) => {
            const dot = document.createElement("span");
            dot.classList.add("product__dot");
            if (indice === 0) dot.classList.add("product__dot--active"); 
            
            // Evento click directo a cada puntito
            dot.addEventListener("click", () => {
                cambiarImagen(indice);
            });

            contenedorDots.appendChild(dot);
        });

        // 3. Asignar los eventos de clic a las flechas de navegación
        if (btnIzquierda) {
            btnIzquierda.onclick = () => {
                cambiarImagen(indiceActual - 1);
            };
        }

        if (btnDerecha) {
            btnDerecha.onclick = () => {
                cambiarImagen(indiceActual + 1);
            };
        }

        // Ocultar flechas si solo hay una imagen (mejora de UX)
        if (imagenes.length <= 1) {
            if (btnIzquierda) btnIzquierda.style.display = "none";
            if (btnDerecha) btnDerecha.style.display = "none";
        } else {
            if (btnIzquierda) btnIzquierda.style.display = "block";
            if (btnDerecha) btnDerecha.style.display = "block";
        }
        // =========================================================================

        // Función que permite calcular el stock neto en el frontend
        const obtenerStockDisponible = (variante) => {
            const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
            const enCarrito = carrito.find(item => item.id === variante.id);
            const cantidadEnCarrito = enCarrito ? enCarrito.cantidad : 0;
            return variante.stock - cantidadEnCarrito;
        };

        if (selectTalla && producto.variantes && producto.variantes.length > 0) {
            selectTalla.innerHTML = ""; // Limpiamos opciones estáticas

            producto.variantes.forEach(variante => {
                const option = document.createElement("option");
                option.value = variante.id; 
                option.textContent = variante.talla; 
                selectTalla.appendChild(option);
            });

            // Inicializamos la interfaz con los datos de la primera variante
            const primeraVariante = producto.variantes[0];
            hiddenIdInput.value = primeraVariante.id;
            txtPrecio.textContent = `Precio: $${primeraVariante.valor.toFixed(2)}`;
            
            let stockDisponibleInicial = obtenerStockDisponible(primeraVariante);
            if (txtStock) txtStock.textContent = `Stock disponible: ${stockDisponibleInicial}`;
            if (inputCantidad) {
                inputCantidad.max = stockDisponibleInicial;
                inputCantidad.value = stockDisponibleInicial > 0 ? 1 : 0; 
            }

            // Evento para cuando el cliente cambie de talla en el select
            selectTalla.addEventListener("change", (e) => {
                const idSeleccionado = parseInt(e.target.value);
                const varianteSeleccionada = producto.variantes.find(v => v.id === idSeleccionado);
                
                if (varianteSeleccionada) {
                    hiddenIdInput.value = varianteSeleccionada.id;
                    txtPrecio.textContent = `Precio: $${varianteSeleccionada.valor.toFixed(2)}`;
                    
                    let stockDisponible = obtenerStockDisponible(varianteSeleccionada);
                    if (txtStock) txtStock.textContent = `Stock disponible: ${stockDisponible}`;
                        
                    if (inputCantidad) {
                        inputCantidad.max = stockDisponible;
                        if (stockDisponible === 0) {
                            inputCantidad.value = 0;
                        } else if (parseInt(inputCantidad.value) > stockDisponible || parseInt(inputCantidad.value) === 0) {
                            inputCantidad.value = 1; 
                        }
                    }
                }
            });
        }

        // 4. Asignamos el evento click al botón añadir al carrito
        if (btnAgregarCarrito && producto.variantes) {
            btnAgregarCarrito.addEventListener("click", () => {
                
                const idVarianteActual = parseInt(hiddenIdInput.value);
                const varianteSeleccionada = producto.variantes.find(v => v.id === idVarianteActual);

                if (!varianteSeleccionada) {
                    alert("Por favor, selecciona una variante válida.");
                    return;
                }

                const cantidadAAgregar = inputCantidad ? parseInt(inputCantidad.value) : 1;

                if (isNaN(cantidadAAgregar) || cantidadAAgregar <= 0) {
                    alert("Por favor, ingresa una cantidad válida mayor a 0.");
                    return;
                }

                let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
                const productoActual = carrito.find(item => item.id === idVarianteActual);
                const cantidadPrevia = productoActual ? productoActual.cantidad : 0;
    
                if (productoActual) {
                    const cantidadTotalProyectada = productoActual.cantidad + cantidadAAgregar;

                    if (cantidadTotalProyectada <= varianteSeleccionada.stock) {
                        productoActual.cantidad = cantidadTotalProyectada;
                    } else {
                        const disponibles = varianteSeleccionada.stock - productoActual.cantidad;
                        alert(`No puedes agregar esa cantidad. Ya tienes ${productoActual.cantidad} en el carrito. Stock máximo disponible restante: ${disponibles}`);
                        return;
                    }
                } else {
                    if (cantidadAAgregar <= varianteSeleccionada.stock) {
                        carrito.push({
                            id: varianteSeleccionada.id, 
                            nombre: producto.nombre,
                            precio: varianteSeleccionada.valor,
                            imagen: (producto.listaImagenes && producto.listaImagenes[0]) || "../images/Rectangle 11.png",
                            talla: varianteSeleccionada.talla, 
                            cantidad: cantidadAAgregar 
                        });
                    } else {
                        alert(`Lo sentimos, no hay suficiente stock. Máximo disponible: ${varianteSeleccionada.stock}`);
                        return;
                    }
                }
    
                localStorage.setItem("carritoSastreria", JSON.stringify(carrito));
                alert(`Se agregaron ${cantidadAAgregar} unidad(es) de ${producto.nombre} (${varianteSeleccionada.talla}) al carrito.`);

                let nuevoStockDisponible = varianteSeleccionada.stock - (cantidadPrevia + cantidadAAgregar);
                if (txtStock) txtStock.textContent = `Stock disponible: ${nuevoStockDisponible}`;
                if (inputCantidad) {
                    inputCantidad.max = nuevoStockDisponible;
                    if (nuevoStockDisponible === 0) inputCantidad.value = 0;
                }
            });
        }
    } catch (error) {
        console.error("Error al cargar detalles: ", error);
    }
}
