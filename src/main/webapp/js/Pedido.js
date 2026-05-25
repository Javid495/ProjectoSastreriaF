import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contFormulario = document.querySelector("#MostraPedido");

document.addEventListener("DOMContentLoaded", async () =>{
    
    await aparecerCont("../");
})

BtnRealizarP.addEventListener("click", async () =>{
    await llamarComponente("#MostraPedido" , "../componentesWeb/FormularioPedidos.html");
    
})


contFormulario.addEventListener("click", (evento) => {
    /* ¿El elemento que tocó el usuario es el botón volver 
       o está metido dentro del botón volver (como el icono <i>)?
    */
    if (evento.target.closest("#btn-volver")) {
        // Vaciamos el contenedor para ocultar el formulario
        contFormulario.innerHTML = "";
    }

    if (evento.target.closest("#btn-Cancelar")){

        contFormulario.innerHTML = "";
    }
});





