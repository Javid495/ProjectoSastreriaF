
export async function CargarDetallesProd(id) {

    const btnAgregarCarrito = document.querySelector("#btnAgregarCarrito");

    try{

        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);
        
        const producto = await respuesta.json();
        console.log("Datos recibidos del Servlet:", producto);
        console.log(producto.imgs);
        
        document.querySelector(".product__title").textContent = producto.nombre;
        
        document.querySelector(".product__price").textContent = `Precio: $${producto.valor}`;
        
        document.querySelector(".product__size").textContent = `Talla: ${producto.talla}`;
        
        document.querySelector(".product__text").textContent = producto.descripcion;
        
        console.log("Encuentra la lista");
        
        // 1. Mostrar UNA imagen en concreto (la primera como principal)
        const imgPrincipal = document.querySelector(".product__image img");
        
        if (imgPrincipal && producto.listaImagenes && producto.listaImagenes.length > 0) {
            const primeraImagen = producto.listaImagenes[0];
            
            // Verificamos si es un enlace externo o local
            if (primeraImagen.startsWith("http://") || primeraImagen.startsWith("https://") || primeraImagen.startsWith("data:")) {
                imgPrincipal.src = primeraImagen;
            } else {
                imgPrincipal.src = ".." + primeraImagen;
            }
            imgPrincipal.alt = producto.nombre;
        }
        
        // 2. Mostrar TODAS (crear los puntitos o miniaturas del carrusel dinámicamente)
        const contenedorDots = document.querySelector(".product__dots");
        contenedorDots.innerHTML = ""; // Limpiamos los estáticos del HTML
        
        if (producto.listaImagenes) {
            producto.listaImagenes.forEach((urlImagen, indice) => {
                // Creamos un puntito indicador por cada imagen que tenga la prenda
                const dot = document.createElement("span");
                dot.classList.add("product__dot");
                if (indice === 0) dot.classList.add("product__dot--active"); // El primero activo
        
                // Resolvemos la ruta correcta para guardarla en el dataset del puntito
                let rutaFinal = "";

                if (urlImagen.startsWith("http://") || urlImagen.startsWith("https://") || urlImagen.startsWith("data:")) {
                    rutaFinal = urlImagen;
                } else {
                    rutaFinal = ".." + urlImagen;
                }
        
                // Le guardamos la ruta ya procesada en el atributo personalizado
                dot.dataset.ruta = rutaFinal; 
        
                contenedorDots.appendChild(dot);
            });
        }
        
    }

    catch (error){
        console.error("Error al cargar detalles: ", error)
    }

    if (btnAgregarCarrito && producto){
        
        //asignamos el evento click al botont añadir al carrito
        btnAgregarCarrito.addEventListener("click", (e) => {
    
            let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

            const prodductoActual = 
        })
    }
} 
