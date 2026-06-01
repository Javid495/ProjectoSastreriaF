import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contPedidos = document.querySelector("#contenedorPedidos");
const contFormulario = document.querySelector("#MostraPedido");
const cuerpoPagina = document.querySelector("body");

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
                <button class="card__button" data-id = "${pedido.id}">Ver detalles</button>
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

        cuerpoPagina.classList.remove("overlay");
        contFormulario.innerHTML = "";
    }
});


// Escuchamos los clics en el contenedor de tarjetas de pedidos
contPedidos.addEventListener("click", async (evento) => {
    const botonDetalles = evento.target.closest(".card__button");
    
    if (botonDetalles) {
        const idPedido = botonDetalles.dataset.id;
        let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

        // 1. Cargamos el componente visual de la ventana emergente en el contenedor de formularios
        await llamarComponente("#MostraPedido", "../componentesWeb/mostrarDetallesPedido.html");

        // 2. Solicitamos los artículos de este pedido al servidor de manera limpia
        fetch(`${urlBase}/ObtenerDetallePedido?idPedido=${idPedido}`)
            .then(response => response.json())
            .then(productos => {
                const contenedorLista = document.querySelector("#listaArticulosPedido");
                let htmlDetalle = "";

                if (productos.length === 0) {
                    htmlDetalle = "<p style='text-align:center; color:#666;'>No se encontraron productos para este pedido.</p>";
                } else {
                    productos.forEach(prod => {
                        // Si la prenda no tiene imagen asignada en la tabla 'imagenes', colocamos una genérica
                        const rutaImg = prod.imagen ? "../" + prod.imagen : "../assets/img/default-prenda.png";
                        const precio = parseFloat(prod.precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                        const total = parseFloat(prod.totalLineal).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                        const cantidad = prod.cantidad || 1;
                        
                        htmlDetalle += `
                            <div class="item-detalle">
                                <img src="${rutaImg}" alt="${prod.nombre}" class="item-imagen">
                                <div style="flex-grow: 1;">
                                    <h4 class="nombre-product">${prod.nombre}</h4>
                                    <p class= "product-details">Unidadeds: $${cantidad}</p>
                                    <p class= "product-details">Precio Base: $${precio}</p>
                                </div>
                                <div style="font-weight: bold; color: #5d2b90; font-size: 13px;">
                                    Subtotal: $${total}
                                </div>
                            </div>
                        `;
                    });
                }
                if (contenedorLista) contenedorLista.innerHTML = htmlDetalle;
            })
            .catch(err => {
                console.error("Error cargando el desglose desde MySQL:", err);
                const contenedorLista = document.querySelector("#listaArticulosPedido");
                if (contenedorLista) contenedorLista.innerHTML = "<p>Ocurrió un error al cargar los datos.</p>";
            });
    }

    cuerpoPagina.classList.add("overlay");

});



