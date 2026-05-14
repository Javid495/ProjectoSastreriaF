
export async function CargarDetallesProd(id) {

    try{

        const respuesta = await fetch(`../ObtenerProductosDetalle?id=${id}`);

       

        const producto = await respuesta.json();
        console.log("Datos recibidos del Servlet:", producto);

        document.querySelector(".product__title").textContent = producto.nombre;

        document.querySelector(".product__price").textContent = `Precio: $${producto.valor}`;

        document.querySelector(".product__size").textContent = `Talla: ${producto.talla}`;

        document.querySelector(".product__text").textContent = producto.descripcion;

        const imgPrincipal = document.querySelector(".product__image img");

        imgPrincipal.src = "../" + producto.imagen;
        imgPrincipal.alt = producto.nombre


    }
    catch (error){
        console.error("Error al cargar detalles: ", error)
    }
} 