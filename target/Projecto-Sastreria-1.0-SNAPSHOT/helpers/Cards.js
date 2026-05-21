
export function crearCards(datos){
    
    //Creacion de los elementos html
    const article = document.createElement("article");
    const imagenCat = document.createElement("img");
    const nombreP = document.createElement("h3");
    const precio = document.createElement("p");
    const boton = document.createElement("button");

    article.classList.add("card");
    imagenCat.classList.add("card__image");
    nombreP.classList.add("card__title");
    precio.classList.add("card__price");
    boton.classList.add("card__button");

    // Le asignamos un evento a los botones de las cards que funcionaran
    // Para redireccionar a los detalles de los productos
    boton.addEventListener("click" , () => {
        window.location.href = `DescripcionProducto.html?id=${datos.id}`
    });

    // Datos mostrados en las cards

    
    if (imagenCat && datos.imagen) {
        // Verificamos si la ruta ya es un enlace completo de internet
        if (datos.imagen.startsWith("http://") || datos.imagen.startsWith("https://") || datos.imagen.startsWith("data:")) {
        // Si es un enlace externo, lo asignamos directamente sin el "../"
            imagenCat.src = datos.imagen;
        } 
        
        else {
            imagenCat.src = ".." + datos.imagen;
            
        }
        imagenCat.alt = datos.nombre;
        console.log(datos.imagen);
    }
    else{
        imagenCat.src = "../images/Rectangle 11.png" ;
    }

    nombreP.textContent = datos.nombre;
    precio.textContent = `Precio: $${datos.valor.toFixed(2)}`;
    boton.innerText= "Detalles";

    //Asignamos datos tecnicos para los filtrados
    article.dataset.categoria = datos.categoria;
    article.dataset.talla = datos.talla;
    article.dataset.visitas = datos.visitas;
    article.dataset.estado = datos.estado;
    article.dataset.id = datos.id

    article.appendChild(imagenCat);
    article.appendChild(nombreP);
    article.appendChild(precio);
    article.appendChild(boton);

    return article;
}



