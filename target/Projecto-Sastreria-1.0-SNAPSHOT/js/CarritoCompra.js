// Botones de accion
const btnRealizarC = document.querySelector("#RealizarCompra");
const btnCancelar = document.querySelector("#cancelarCompra");
const btnConfirmarC = document.querySelector("#ConfirmarCompra");
const btnPagoConfirmado = document.querySelector("#pagoConfirmado")

// Manipulacion de ventanas
const confirmarComprar = document.querySelector(".compra__carrito");
const sombreado = document.querySelector(".sombreado");
const pagoConfirm = document.querySelector(".confirmacion__pago");

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