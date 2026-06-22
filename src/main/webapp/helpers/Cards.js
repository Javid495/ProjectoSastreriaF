export function crearCards(datos, esRaiz = false) {
    
    // Creación de los elementos HTML
    const article = document.createElement("article");
    const imagenCat = document.createElement("img");
    const nombreP = document.createElement("h3");
    const precio = document.createElement("p");
    const Stock = document.createElement("p");
    const boton = document.createElement("button");

    article.classList.add("card");
    imagenCat.classList.add("card__image");
    nombreP.classList.add("card__title");
    Stock.classList.add("card__price");
    precio.classList.add("card__price");
    boton.classList.add("card__button");

    // 🔀 CONTROL DE RUTAS DE REDIRECCIÓN:
    // Si estás en la raíz, debes entrar a VistasCliente. Si estás dentro, accedes directo.
    boton.addEventListener("click" , () => {
        const rutaDetalle = esRaiz 
            ? `VistasCliente/DescripcionProducto.html?id=${datos.id}` 
            : `DescripcionProducto.html?id=${datos.id}`;
        window.location.href = rutaDetalle;
    });

    // 🖼️ CONTROL DE RUTAS DE IMÁGENES:
    if (imagenCat && datos.imagen) {
        // Verificamos si la ruta ya es un enlace completo de internet
        if (datos.imagen.startsWith("http://") || datos.imagen.startsWith("https://") || datos.imagen.startsWith("data:")) {
            imagenCat.src = datos.imagen;
        } 
        else {
            // Si estás en la raíz, las imágenes se buscan desde el directorio local directamente.
            // Si no, subes un nivel con ".."
            imagenCat.src = esRaiz ? "./" + datos.imagen : ".." + datos.imagen;
        }
        imagenCat.alt = datos.nombre;
    }
    else {
        imagenCat.src = esRaiz ? "images/Rectangle 11.png" : "../images/Rectangle 11.png";
    }

    nombreP.textContent = datos.nombre;
    // Formateamos el precio para conservar la estética de puntos de miles (Ej: 40.000)
    const precioFormateado = Number(datos.valor).toLocaleString('de-DE', { minimumFractionDigits: 0 });
    precio.textContent = `Precio: $${precioFormateado}`;
    
    Stock.textContent = `Stock Disponible: ${datos.stock}`;
    boton.innerText = "Ver Detalles";

    // Asignamos datos técnicos para los filtrados
    article.dataset.categoria = datos.categoria;
    article.dataset.talla = datos.talla;
    article.dataset.visitas = datos.visitas;
    article.dataset.estado = datos.estado;
    article.dataset.id = datos.id;

    article.appendChild(imagenCat);
    article.appendChild(nombreP);
    article.appendChild(precio);
    article.appendChild(Stock);
    article.appendChild(boton);

    return article;
}



