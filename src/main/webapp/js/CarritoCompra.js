import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { llamarComponente } from "../helpers/CompHtml.js";


// Botones de accion
const btnRealizarC = document.querySelector("#RealizarCompra");

// Manipulacion de ventanas
const confirmarComprar = document.querySelector("#compraCarrito");
const sombreado = document.querySelector(".sombreado");
const pagoConfirm = document.querySelector("#confirmacionPago");

const ContPrecio = document.querySelector(".precio__titulo");

// Funcion para la renderizacion del carrito
function rederizarCarrito(){

    // Hacemos referencia al contenedor principal
    const contenedor = document.querySelector("#mostrarCompra");

    // obtenemos los elementos de localstorage
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

    // En caso de que no haya ningun producto en el carrito 
    if (carrito.length === 0){
        contenedor.innerHTML = "<p>Tu carrito actualmente se encuentra vacio</p>"
        return
    }

    // variable que almacena el total de la compra de los productos
    let totalCompra = 0;

    //se limpia el contenedor principal
    contenedor.innerHTML = "";

    carrito.forEach(producto => {
        
        //Primero solucionamos la ruta de la imagen 
        const rutaImg = (producto.imagen.startsWith("http")) || producto.imagen.startsWith("data:") ? producto.imagen
        : ".." + producto.imagen;

        const subtotal = producto.precio * producto.cantidad;
        totalCompra += subtotal;

        const divItem = document.createElement("div");
        divItem.classList.add("popular__cards");
        divItem.innerHTML = `
            <article class="card">
                <img src="${rutaImg}" alt="Prenda A" class="card__image">
                <h3 class="card__title">${producto.nombre}</h3>
                <p class="card__price">${producto.precio.toLocaleString()}</p>
                <p class = "card__price">Cantidad: ${producto.cantidad}</p>
                <button class="button__closed" data-id="${producto.id}">X</button>
            </article>

        `
        const btnCerrar = divItem.querySelector(".button__closed");

        console.log(btnCerrar);

        btnCerrar.addEventListener("click", (e) =>{
            const ProductoEliminar = parseInt(e.target.dataset.id);
            console.log(ProductoEliminar);
            eliminarProducto(ProductoEliminar);
        })

        const precio = document.createElement("p");
        precio.classList.add("precio__prenda");
        precio.id = `id${producto.id}`;
        precio.innerText = `$${producto.precio.toLocaleString()}`

        ContPrecio.append(precio);

        contenedor.appendChild(divItem);
    });



    document.querySelector("#precio__total").textContent = `$${totalCompra}`
}

// Funcion pricinpal de eliminar carrito
function eliminarProducto(id){

    const precioDelet = document.getElementById(`id${id}`);

    precioDelet.remove();   
    let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
    carrito = carrito.filter(item => item.id !== id);
    localStorage.setItem("carritoSastreria", JSON.stringify(carrito));
    
    document.querySelector("#precio__total").textContent = ``;
    rederizarCarrito();
}


document.addEventListener("DOMContentLoaded", async () => {
    await aparecerCont("../");
    await rederizarCarrito();
})



// Asignamos eventos a los botones del apartado
btnRealizarC.addEventListener("click", async (e) => {
    sombreado.classList.add("aparecerSombreado");
    await llamarComponente("#compraCarrito", "../componentesWeb/FormulairoCompra.html");
});

// CORREGIDO: Delegación de eventos para Confirmar/Cancelar Compra
confirmarComprar.addEventListener("click", async (e) => {
    
    // Si presiona Cancelar
    if (e.target.closest("#cancelarCompra")) {
        sombreado.classList.remove("aparecerSombreado");
        confirmarComprar.innerHTML = "";
    }


    if (e.target.closest("#ConfirmarCompra")) {
        confirmarComprar.innerHTML = "";
        await llamarComponente("#confirmacionPago", "../componentesWeb/VentanaComprobacion.html");
    }
});

//Delegación de eventos para la ventana de Pago Confirmado
pagoConfirm.addEventListener("click", (e) => {
    
 
    if (e.target.closest("#pagoConfirmado")) {
        sombreado.classList.remove("aparecerSombreado");
        pagoConfirm.innerHTML = "";
    }
})



