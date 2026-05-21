// Botones de accion
const btnRealizarC = document.querySelector("#RealizarCompra");
const btnCancelar = document.querySelector("#cancelarCompra");
const btnConfirmarC = document.querySelector("#ConfirmarCompra");
const btnPagoConfirmado = document.querySelector("#pagoConfirmado")

// Manipulacion de ventanas
const confirmarComprar = document.querySelector(".compra__carrito");
const sombreado = document.querySelector(".sombreado");
const pagoConfirm = document.querySelector(".confirmacion__pago");


function rederizarCarrito(){

    const contenedor = document.querySelector("#mostrarCompra");

    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

    if (carrito.length === 0){
        contenedor.innerHTML = "<p>Tu carrito actualmente se encuentra vacio</p>"
        return
    }

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
                <img src="images/Rectangle 11.png" alt="Prenda A" class="card__image">
                <h3 class="card__title">Prenda A</h3>
                <p class="card__price">Precio: 40.000</p>
                <button class="card__button">Ver detalles</button>
                <button class="button__closed">X</button>
            </article>
        `

    });
}


document.addEventListener("DOMContentLoaded", () => {
    
})

//asignamos eventos a los botones del apartado
btnRealizarC.addEventListener("click", (e) => {
    
    e.preventDefault();

    sombreado.classList.add("aparecerSombreado");

    confirmarComprar.classList.add("mostarConfirmacion")
    
})

btnCancelar.addEventListener("click", (e) => {

    e.preventDefault();

    sombreado.classList.remove("aparecerSombreado");

    confirmarComprar.classList.remove("mostarConfirmacion");
})

btnConfirmarC.addEventListener("click", (e) =>{

    e.preventDefault();

    confirmarComprar.classList.remove("mostarConfirmacion");

    pagoConfirm.classList.add("mostarConfirmacion");
})

btnPagoConfirmado.addEventListener("click", (e) =>{

    e.preventDefault();

    sombreado.classList.remove("aparecerSombreado");

    pagoConfirm.classList.remove("mostarConfirmacion");
})