import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";
import { RealizarCompra } from "../helpers/ProcesoCompra.js";

const btnRealizarC = document.querySelector("#RealizarCompra");
const confirmarComprar = document.querySelector("#compraCarrito");
const sombreado = document.querySelector(".sombreado");
const pagoConfirm = document.querySelector("#confirmacionPago");
const ContPrecio = document.querySelector(".precio__titulo");

function rederizarCarrito(){
    const contenedor = document.querySelector("#mostrarCompra");
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

    if (carrito.length === 0){
        contenedor.innerHTML = "<p>Tu carrito actualmente se encuentra vacío</p>";
        return;
    }

    let totalCompra = 0;
    contenedor.innerHTML = "";
    ContPrecio.innerHTML = ""; // Limpiamos subtotales previos para evitar duplicados

    carrito.forEach(producto => {
        const rutaImg = (producto.imagen.startsWith("http")) || producto.imagen.startsWith("data:") 
            ? producto.imagen : ".." + producto.imagen;

        const subtotal = producto.precio * producto.cantidad;
        totalCompra += subtotal;

        const divItem = document.createElement("div");
        divItem.classList.add("popular__cards");
        divItem.innerHTML = `
            <article class="card">
                <img src="${rutaImg}" alt="Prenda" class="card__image">
                <h3 class="card__title">${producto.nombre}</h3>
                <p class="card__price">$${producto.precio.toLocaleString()}</p>
                <p class="card__price">Cantidad: ${producto.cantidad}</p>
                <button class="button__closed" data-id="${producto.id}">X</button>
            </article>
        `;

        divItem.querySelector(".button__closed").addEventListener("click", (e) =>{
            const ProductoEliminar = parseInt(e.target.dataset.id);
            eliminarProducto(ProductoEliminar);
        });

        // CORREGIDO: Se calcula la multiplicación ANTES de convertir a string local
        const precio = document.createElement("p");
        precio.classList.add("precio__prenda");
        precio.id = `id${producto.id}`;
        precio.innerText = `$${(producto.precio * producto.cantidad).toLocaleString()}`;

        ContPrecio.append(precio);
        contenedor.appendChild(divItem);
    });

    document.querySelector("#precio__total").textContent = `$${totalCompra.toLocaleString()}`;
}

function eliminarProducto(id){
    const precioDelet = document.getElementById(`id${id}`);
    if(precioDelet) precioDelet.remove();   

    let carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
    carrito = carrito.filter(item => item.id !== id);
    localStorage.setItem("carritoSastreria", JSON.stringify(carrito));
    
    rederizarCarrito();
}

document.addEventListener("DOMContentLoaded", async () => {
    await aparecerCont("../");
    await comprobarSesion("../");
    await rederizarCarrito();
});

// Desplegar Formulario de Compra
btnRealizarC.addEventListener("click", async (e) => {
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
    if(carrito.length === 0) {
        alert("Agrega productos antes de proceder al pago.");
        return;
    }
    sombreado.classList.add("aparecerSombreado");
    await llamarComponente("#compraCarrito", "../componentesWeb/FormulairoCompra.html");
    
    // Ejecuta las funciones internas del formulario inmediatamente
    await RealizarCompra();
});

// Delegación de eventos EXCLUSIVAMENTE para Cancelar Compra
confirmarComprar.addEventListener("click", (e) => {
    if (e.target.closest("#cancelarCompra")) {
        sombreado.classList.remove("aparecerSombreado");
        confirmarComprar.innerHTML = "";
    }
    
});

// Cerrar ventana de Pago Confirmado
pagoConfirm.addEventListener("click", (e) => {
    if (e.target.closest("#pagoConfirmado")) {
        sombreado.classList.remove("aparecerSombreado");
        pagoConfirm.innerHTML = "";
        rederizarCarrito(); // Recargamos la vista (ahora saldrá vacía)
    }
});



