import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contPedidos = document.querySelector("#contenedorPedidos");
const contFormulario = document.querySelector("#MostraPedido");

function MostrarPedidosUser() {

    let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

    fetch(`${urlBase}/ObtenerPedidos`)
    .then(response => response.json())
    .then(data => {
        
        if(!data.logeado){
            contPedidos.innerHTML = `
                <h1>Se require un inicio de sesion html</h1>
            `;

            return;
        }

        if (data.pedidos.length === 0){
            contPedidos.innerHTML = "<p style='text-align:center; padding: 20px;'>Aún no has realizado pedidos en Moda Suescún.</p>";

            return;
        }

        data.pedidos.forEach(pedido => {
            
            contPedidos.innerHTML += `
            <article class="card">
                <h3 class="card__title">Pedido: ${pedido.fecha}</h3>
                <p class="card__estado">Estado: ${pedido.estado}</p>
                <p class="card__tipocompra">Tipo de Compra: ${pedido.tipo}</p>
                <button class="card__button">Ver detalles</button>
            </article> `
        });
    })
    .catch(error => console.error("Error con la conexion de los pedidos:", error));
}

document.addEventListener("DOMContentLoaded", async () =>{
    
    await aparecerCont("../");

    comprobarSesion("../");

    MostrarPedidosUser();
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





