import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contPedidos = document.querySelector("#contenedorPedidos");
const contFormulario = document.querySelector("#MostraPedido");
const cuerpoPagina = document.querySelector("body");
const filtroEstadoContainer = document.querySelector("#filtro-estado-container");

// 🌐 Definimos la urlBase de forma global en el archivo
const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

/**
 * 🔄 Cambiar entre Pedidos de Catálogo (Pendientes) y Pedidos Personalizados (Cotizados)
 * Se expone a window para que los botones con 'onclick' del HTML puedan ejecutarla.
 */
window.cambiarTipoPedido = function(tipo, elemento) {
    // 1. Gestionar clases visuales de las pestañas
    document.querySelectorAll(".tab-tipo").forEach(btn => btn.classList.remove("active"));
    elemento.classList.add("active");

    // 2. Limpiar el contenedor antes de renderizar la nueva sección
    contPedidos.innerHTML = "";

    // 3. Decidir qué datos cargar y si se muestra el filtro de estados
    if (tipo === 'catalogo') {
        filtroEstadoContainer.style.display = "block"; // El filtro aplica para compras estándar
        MostrarPedidosUser();
    } else if (tipo === 'medida') {
        filtroEstadoContainer.style.display = "none";  // Ocultamos el filtro para las cotizaciones directas
        cargarMisCotizaciones();
    }
}

// 📦 VISTA 1: Pedidos estándar / en proceso (Compras de catálogo)
function MostrarPedidosUser() {
    fetch(`${urlBase}/ObtenerPedidos`)
    .then(response => response.json())
    .then(data => {
        if(!data.logeado){
            contPedidos.innerHTML = `<h1>Se requiere un inicio de sesion html</h1>`;
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
            </article> `;
        });
    })
    .catch(error => console.error("Error con la conexion de los pedidos:", error));
}

// ✨ VISTA 2: Diseños a medida que ya tienen respuesta del sastre (Cotizados)
function cargarMisCotizaciones() {
    fetch(`${urlBase}/MisCotizaciones`)
        .then(res => {
            if (!res.ok) throw new Error("No autorizado o error de servidor");
            return res.json();
        })
        .then(cotizaciones => {
            if (cotizaciones.length === 0) {
                contPedidos.innerHTML = "<p style='text-align:center; padding: 20px;'>Aún no tienes respuestas de cotizaciones pendientes.</p>";
                return;
            }

            cotizaciones.forEach(c => {
                const foto = c.imagen ? `${urlBase}/${c.imagen}` : `${urlBase}/images/Perfil/Ellipse 14.png`;
                const precioFormateado = parseFloat(c.precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });

                // Inyectamos respetando milimétricamente tus clases CSS estructuradas en el HTML
                contPedidos.innerHTML += `
                    <article class="card card--cotizacion">
                        <div class="card__status-tag status--cotizado">¡Cotizado por el Sastre!</div>
                        <h2 class="card__title">Traje a Medida: ${c.tipo}</h2>
                        <p class="card__meta">Tela propuesta: ${c.tela} | Tus medidas: ${c.medidas}</p>
                        <p class="card__meta" style="color: #666; margin-bottom: 8px;"><strong>Detalles:</strong> ${c.descripcion}</p>
                        
                        ${c.imagen ? `<img src="${foto}" style="width:90px; height:90px; object-fit:cover; margin-bottom: 12px; border-radius:6px; border: 1px solid #ddd;">` : ''}
                        
                        <div class="card__propuesta-economica">
                            <span class="cotizacion__precio">$${precioFormateado}</span>
                            <p class="cotizacion__nota">"${c.comentario}"</p>
                            <p style="margin: 4px 0 0 0; font-size: 12px; color: #cc0000; font-weight: bold;">Oferta válida hasta: ${c.fechaLimite}</p>
                        </div>

                        <div class="card__cotizacion-acciones">
                            <button class="btn-cotizacion btn-cotizacion--rechazar" onclick="eliminarCotizacion(${c.id})">Rechazar</button>
                            <button class="btn-cotizacion btn-cotizacion--aceptar" onclick="procederAlPago(${c.id}, ${c.precio})">Aceptar y Pagar</button>
                        </div>
                    </article>
                `;
            });
        })
        .catch(err => console.error("Error cargando cotizaciones:", err));
}

// ⏳ Inicialización del documento
document.addEventListener("DOMContentLoaded", async () =>{
    await aparecerCont("../");
    comprobarSesion("../");

    // Por defecto, al entrar la pestaña activa es 'catalogo' (Pedidos Pendientes)
    MostrarPedidosUser();
});

// [Tus listeners para registrar pedidos y ver detalles se mantienen exactamente igual aquí abajo]
BtnRealizarP.addEventListener("click", async () => { /* Tu lógica de modal intacta */ });
contFormulario.addEventListener("click", (evento) => { /* Tu lógica de botones volver/cancelar intacta */ });
contPedidos.addEventListener("click", async (evento) => { /* Tu lógica de abrir detalles de artículos intacta */ });



