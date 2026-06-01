export function CardPrendasAdmin(datos){

    const Article = document.createElement("article");
    //Elementos de imagen
    const divImagen = document.createElement("div");
    const contImagen = document.createElement("img");

    // Informacion de prendas
    const divInfoPrenda = document.createElement("div");
    const Nombre = document.createElement("p");
    const Precio = document.createElement("p");
    const stockPrenda = document.createElement("p");

    //Contenedor Botones
    const divBotonesFun = document.createElement("div");
    const btnModificar = document.createElement("button");
    const btnEliminar = document.createElement("button");

    //Contenedor de checkbox eliminar varios
    const divCheckbox = document.createElement("div");
    const check = document.createElement("input");

    //Añadir las clases a los elementos
    Article.classList.add("card-inventario");
    divImagen.classList.add("img-placeholder");
    divBotonesFun.classList.add("acciones-prenda");
    btnModificar.classList.add("btn-blanco");
    btnEliminar.classList.add("btn-blanco");
    divCheckbox.classList.add("checkbox-container");
    check.classList.add("custom-checkbox");

    //Agrego el identificador de los botones
    btnModificar.textContent = "Modificar Detalles";
    btnEliminar.textContent = "Eliminar Prenda";
    btnModificar.classList.add ("btnModificarPrenda");
    btnEliminar.classList.add ("btnEliminarPrenda");

    //Agrego los atributos del input
    check.setAttribute("type", "checkbox");
    check.setAttribute("name", "productosSeleccionados");
    check.setAttribute("value", datos.id);

    // Agrego un dataset a las prendas para identificarlas mas adelante
    Article.dataset.id = datos.id;
    Article.dataset.estado = datos.estado;
    Article.dataset.categoria = datos.categoria;
    Article.dataset.talla = datos.talla;
    Article.dataset.visitas = datos.visitas;


    //Funcion del boton eliminar
    btnModificar.addEventListener("click" , () => {
        window.location.href = `VistaModificacionProduct.html?id=${datos.id}`
    });

    Nombre.textContent = datos.nombre;
    Precio.textContent  = ` \nValor: $${datos.valor}`;
    stockPrenda.textContent = `\nStock: ${datos.stock}`;

    if (contImagen && datos.imagen) {
        // Verificamos si la ruta ya es un enlace completo de internet
        if (datos.imagen.startsWith("http://") || datos.imagen.startsWith("https://") || datos.imagen.startsWith("data:")) {
        // Si es un enlace externo, lo asignamos directamente sin el "../"
            contImagen.src = datos.imagen;
        } 
        
        else {
            contImagen.src = ".." + datos.imagen;
            
        }
        contImagen.alt = datos.nombre;
    }
    else{
        contImagen.src = "../images/Rectangle 11.png" ;
    }

    divImagen.appendChild(contImagen);

    divInfoPrenda.appendChild(Nombre);
    divInfoPrenda.appendChild(Precio);
    divInfoPrenda.appendChild(stockPrenda);

    divBotonesFun.appendChild(btnModificar);
    divBotonesFun.appendChild(btnEliminar);

    divCheckbox.appendChild(check)

    Article.appendChild(divImagen);
    Article.appendChild(divInfoPrenda);
    Article.appendChild(divBotonesFun);
    Article.appendChild(divCheckbox);

    return Article;
}