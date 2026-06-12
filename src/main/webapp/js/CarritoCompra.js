import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";
import { RealizarCompra } from "../helpers/ProcesoCompra.js";

const btnRealizarC = document.querySelector("#RealizarCompra");
const confirmarComprar = document.querySelector("#compraCarrito");
const sombreado = document.querySelector(".sombreado");
const pagoConfirm = document.querySelector("#confirmacionPago");
const ContPrecio = document.querySelector(".precio__prenda");

export function rederizarCarrito(){
    const contenedor = document.querySelector("#mostrarCompra");
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

    if (carrito.length === 0){
        contenedor.innerHTML = "<p>Tu carrito actualmente se encuentra vacío</p>";
        if (ContPrecio) ContPrecio.innerHTML = "";
        document.querySelector("#precio__total").textContent = "$0";
        return;
    }

    let totalCompra = 0;
    contenedor.innerHTML = "";
    ContPrecio.innerHTML = ""; 

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
                <p class="card__price">Precio: $${producto.precio.toLocaleString()}</p>
                <p class="card__size">Talla: <strong>${producto.talla}</strong></p> 
                <p class="card__quantity">Cantidad: ${producto.cantidad}</p>
                <p class="card__subtotal">Subtotal: $${subtotal.toLocaleString()}</p> 
                <button class="button__closed" data-id="${producto.id}">X</button>
            </article>
        `;

        divItem.querySelector(".button__closed").addEventListener("click", (e) =>{
            const ProductoEliminar = parseInt(e.target.dataset.id);
            eliminarProducto(ProductoEliminar);
        });

        const precio = document.createElement("p");
        precio.classList.add("precio__prenda");
        precio.id = `id${producto.id}`;
        precio.innerText = `${producto.nombre} (${producto.talla}): $${subtotal.toLocaleString()}`;

        ContPrecio.append(precio);
        contenedor.appendChild(divItem);
    });

    document.querySelector("#precio__total").textContent = `$${totalCompra.toLocaleString()}`;
}

function eliminarProducto(id){
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

// =================================================================
// 🚀 EVENTO DE DESPLIEGUE Y GUARDADO TEMPORAL EN BD
// =================================================================
btnRealizarC.addEventListener("click", async (e) => {
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
    if(carrito.length === 0) {
        alert("Agrega productos antes de proceder al pago.");
        return;
    }

    // 🔒 1. Esperamos la verificación de sesión del servidor
    const sesionActiva = await comprobarSesion("../"); 

    if (!sesionActiva) {
        alert("Para proceder con la compra de tus prendas, por favor inicia sesión.");
        window.location.href = "../inicioSecion.html"; 
        return;
    }

    // 💾 2. NUEVO: Enviamos el estado del LocalStorage al Servlet en modo 'temporal'
    const payloadTemporal = {
        accion: "temporal",
        productos: carrito.map(item => ({
            idPrenda: item.id,
            cantidad: item.cantidad || 1
        }))
    };

    try {
        const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
        const respuesta = await fetch(`${urlBase}/ProcesarCompraServlet`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payloadTemporal)
        });

        const resultado = await respuesta.json();

        // Si la base de datos falla por alguna razón transaccional, detenemos el flujo aquí
        if (resultado.status !== "Exito") {
            alert("No se pudo preparar la orden en el servidor: " + resultado.mensaje);
            return;
        }
        
        console.log("💾 Éxito: Registro del carrito guardado de forma temporal en la Base de Datos.");

    } catch (error) {
        console.error("Error crítico en la comunicación temporal:", error);
        alert("Ocurrió un error de red al intentar sincronizar tu carrito.");
        return;
    }

    // 🔓 3. Si todo salió bien, procedemos a abrir el Modal e inicializar el formulario de pago
    sombreado.classList.add("aparecerSombreado");
    await llamarComponente("#compraCarrito", "../componentesWeb/FormulairoCompra.html");

    // Llama a la función definitiva (la que modificamos en el paso anterior que enviará la 'accion':'confirmar')
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
// pagoConfirm.addEventListener("click", (e) => {
//     if (e.target.closest("#pagoConfirmado")) {
//         sombreado.classList.remove("aparecerSombreado");
//         pagoConfirm.innerHTML = "";
//         rederizarCarrito(); // Recargamos la vista (ahora saldrá vacía)
//     }
// });



