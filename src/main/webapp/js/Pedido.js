import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";
import { RealizarCompra } from "../helpers/ProcesoCompra.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contPedidos = document.querySelector("#contenedorPedidos");
const contFormulario = document.querySelector("#MostraPedido");
const cuerpoPagina = document.querySelector("body");
const filtroEstadoContainer = document.querySelector("#filtro-estado-container");

const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
let pedidosCache = [];

/**
 * Cambiar entre Pedidos de Catálogo y Pedidos Personalizados
 */
window.cambiarTipoPedido = function(tipo, elemento) {
    document.querySelectorAll(".tab-tipo").forEach(btn => btn.classList.remove("active"));
    elemento.classList.add("active");

    contPedidos.innerHTML = ""; // Limpieza al cambiar de pestaña

    if (tipo === 'catalogo') {
        if (filtroEstadoContainer) filtroEstadoContainer.style.display = "block";
        MostrarPedidosUser();
    } else if (tipo === 'medida') {
        if (filtroEstadoContainer) filtroEstadoContainer.style.display = "none";
        cargarMisCotizaciones();
    }
}

// 📦 VISTA 1: Obtener Pedidos Estándar y a Medida ya procesados
function MostrarPedidosUser() {
    contPedidos.innerHTML = ""; 

    fetch(`${urlBase}/ObtenerPedidos`)
    .then(response => response.json())
    .then(data => {
        if(!data.logeado){
            contPedidos.innerHTML = `<h1 style='text-align:center; padding: 20px;'>Se requiere iniciar sesión para ver esta sección.</h1>`;
            return;
        }

        if (data.pedidos.length === 0){
            contPedidos.innerHTML = "<p style='text-align:center; padding: 20px;'>Aún no has realizado pedidos en Moda Suescún.</p>";
            return;
        }
    
        pedidosCache = data.pedidos;

        data.pedidos.forEach(pedido => {
            let tituloDiferenciador = "";
            if (pedido.tipo.toLowerCase().includes("medida") && pedido.detalleMedida) {
                tituloDiferenciador = `Confección: ${pedido.detalleMedida.tipoPrenda}`;
            } else {
                tituloDiferenciador = `Pedido de Catálogo (Día ${pedido.fecha})`;
            }

            contPedidos.innerHTML += `
            <article class="card">
                <div class="card__status-tag text-uppercase">${pedido.estado}</div>
                <h3 class="card__title" style="margin-top: 10px;">${tituloDiferenciador}</h3>
                <p class="card__estado"><strong>Fecha de Solicitud:</strong> ${pedido.fecha}</p>
                <p class="card__tipocompra"><strong>Modalidad:</strong> ${pedido.tipo}</p>
                <button class="card__button" data-id="${pedido.id}">Ver detalles del pedido</button>
            </article> `;
        });
    })
    .catch(error => console.error("Error con la conexión de los pedidos:", error));
}

// ✨ VISTA 2: Obtener Respuestas de Diseños desde Cero
function cargarMisCotizaciones() {
    contPedidos.innerHTML = ""; 

    fetch(`${urlBase}/MisCotizaciones`)
        .then(res => {
            if (!res.ok) throw new Error("No autorizado o error de servidor");
            return res.json();
        })
        .then(cotizaciones => {
            // 🔍 DIAGNÓSTICO: Abre la consola para ver si el Backend envía CotizacionPedido_Id
            console.log("👉 Datos crudos recibidos de /MisCotizaciones:", cotizaciones);

            if (cotizaciones.length === 0) {
                contPedidos.innerHTML = `<p style="text-align:center; color:#666; padding: 20px;">Aún no tienes respuestas de cotizaciones pendientes.</p>`;
                return;
            }

            cotizaciones.forEach(c => {
                const foto = c.imagen ? `${urlBase}/${c.imagen}` : `${urlBase}/images/Perfil/Ellipse 14.png`;
                const precioFormateado = parseFloat(c.precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                
                contPedidos.innerHTML += `
                    <article class="card card--cotizacion">
                        <div class="card__status-tag status--cotizado">¡Cotizado por el Sastre!</div>
                        <h2 class="card__title">Traje a Medida: ${c.tipo}</h2>
                        <p class="card__meta">Tela propuesta: ${c.tela} | Tus medidas: ${c.medidas}</p>
                        <p class="card__meta" style="color: #666; margin-bottom: 8px;"><strong>Detalles del diseño:</strong> ${c.descripcion}</p>
                        
                        ${c.imagen ? `<img src="${foto}" style="width:90px; height:90px; object-fit:cover; margin-bottom: 12px; border-radius:6px; border: 1px solid #ddd;">` : ''}
                        
                        <div class="card__propuesta-economica">
                            <span class="cotizacion__precio">$${precioFormateado}</span>
                            <p class="cotizacion__nota">"${c.comentario}"</p>
                            <p style="margin: 4px 0 0 0; font-size: 12px; color: #cc0000; font-weight: bold;">Oferta válida hasta: ${c.fechaLimite}</p>
                        </div>
                        
                        <div class="card__cotizacion-acciones">
                            <button class="btn-cotizacion btn-cotizacion--rechazar" data-id="${c.id}">Rechazar</button>
                            <button class="btn-cotizacion btn-cotizacion--aceptar" 
                                    data-id-cotizacion="${c.CotizacionPedido_Id || c.id}" 
                                    data-precio="${c.precio}">
                                Aceptar y Pagar
                            </button>
                        </div>
                    </article>
                `;
            });
        })
        .catch(err => console.error("Error cargando cotizaciones:", err));
}

// ⏳ Inicialización base de la página
document.addEventListener("DOMContentLoaded", async () =>{
    await aparecerCont("../");
    comprobarSesion("../");
    MostrarPedidosUser();
});

// 📋 BOTÓN PRINCIPAL: Solo carga el componente HTML del formulario
BtnRealizarP.addEventListener("click", async () => {
    await llamarComponente("#MostraPedido" , "../componentesWeb/FormularioPedidos.html");
});

// 🚀 ESCUCHADOR DE SUBMIT: Registro de solicitudes personalizadas
document.addEventListener("submit", async (evento) => {
    if (evento.target.matches("#form-solicitud-personalizada")) {
        evento.preventDefault();
        
        const formulario = evento.target;
        const formData = new FormData();

        formData.append("tipoPrenda", document.querySelector("#tipo-prenda").value);
        formData.append("telas", document.querySelector("#sugerencia-telas").value);
        formData.append("talla", document.querySelector("#talla-prenda").value);
        formData.append("descripcion", document.querySelector("#desc-pedido").value);

        const inputsMedidas = document.querySelectorAll(".medidas-inputs .input-dark");
        const arrayMedidas = [];
        inputsMedidas.forEach(input => {
            if(input.value) arrayMedidas.push(input.value);
        });
        formData.append("medidas", arrayMedidas.join(","));

        const inputFile = document.querySelector("#file-upload");
        if (inputFile.files.length > 0) {
            formData.append("fotoReferencia", inputFile.files[0]);
        }

        fetch(`${urlBase}/RegistrarPedidoMedida`, {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert("¡Tu solicitud de diseño ha sido enviada con éxito!");
                document.querySelector("#MostraPedido").innerHTML = ""; 
                MostrarPedidosUser(); 
            } else {
                alert("Error al procesar la solicitud: " + data.mensaje);
            }
        })
        .catch(err => console.error("Error en el envío del diseño a medida:", err));
    }
});

// 🗑️ Escucha global de eventos para RECHAZAR y ELIMINAR cotizaciones
document.addEventListener("click", async (e) => {
    if (e.target.matches(".btn-cotizacion--rechazar")) {
        const boton = e.target;
        const idPedidoMedida = boton.dataset.id;

        const confirmarRechazo = confirm("¿Estás seguro de que deseas rechazar esta cotización? Esta acción eliminará permanentemente tu solicitud de diseño.");
        if (!confirmarRechazo) return;

        const params = new URLSearchParams();
        params.append("idPedidoMedida", idPedidoMedida);
        params.append("accion", "eliminar");

        fetch(`${urlBase}/ResponderCotizacion`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
        .then(res => {
            if (!res.ok) throw new Error("Error en la respuesta del servidor");
            return res.json();
        })
        .then(data => {
            if (data.success) {
                alert("La cotización y la solicitud han sido eliminadas correctamente.");
                cargarMisCotizaciones(); 
            } else {
                alert("No se pudo procesar la eliminación: " + data.mensaje);
            }
        })
        .catch(err => {
            console.error("Error crítico al intentar eliminar la cotización:", err);
            alert("Ocurrió un error de red al procesar tu solicitud.");
        });
    }
});

// ❌ INTERFAZ MODAL: Controladores para vaciar/cerrar modales abiertos
contFormulario.addEventListener("click", (evento) => {
    if (evento.target.closest("#btn-volver")) {
        contFormulario.innerHTML = "";
    }
    if (evento.target.closest("#btn-Cancelar")){
        const capaSombreado = document.querySelector(".sombreado");
        if (capaSombreado) capaSombreado.classList.remove("aparecerSombreado");
        contFormulario.innerHTML = "";
    }
});

// TARJETAS DE PEDIDOS: Apertura y desglose dinámico inteligente desde la Caché local
contPedidos.addEventListener("click", async (evento) => {
    const botonDetalles = evento.target.closest(".card__button");
    
    if (botonDetalles) {
        const idPedido = parseInt(botonDetalles.dataset.id);
        const pedidoSeleccionado = pedidosCache.find(p => p.id === idPedido);

        if (!pedidoSeleccionado) {
            console.error("No se encontró el pedido en la caché local.");
            return;
        }

        await llamarComponente("#MostraPedido", "../componentesWeb/mostrarDetallesPedido.html");
        const contenedorLista = document.querySelector("#listaArticulosPedido");
        let htmlDetalle = "";

        if (pedidoSeleccionado.detalleMedida) {
            const deMedida = pedidoSeleccionado.detalleMedida;
            const precioTotal = parseFloat(deMedida.valor).toLocaleString('es-CO', { minimumFractionDigits: 0 });
            
            htmlDetalle = `
                <div class="item-detalle-medida" style="padding: 15px; border-left: 4px solid #5d2b90; background: #fdfbff;">
                    <h4 style="color: #5d2b90; margin-bottom: 8px; font-size: 16px;">Especificaciones de Confección</h4>
                    <p style="margin: 4px 0;"><strong>Prenda Solicitada:</strong> ${deMedida.tipoPrenda}</p>
                    <p style="margin: 4px 0;"><strong>Material / Tela:</strong> ${deMedida.tela}</p>
                    <p style="margin: 4px 0; color: #555;"><strong>Notas del Sastre:</strong> <em>"${deMedida.comentario || 'Sin observaciones adicionales.'}"</em></p>
                    <div style="margin-top: 15px; font-weight: bold; font-size: 15px; color: #5d2b90; text-align: right;">
                        Total Confección: $${precioTotal}
                    </div>
                </div>
            `;
        } else if (pedidoSeleccionado.productos && pedidoSeleccionado.productos.length > 0) {
            pedidoSeleccionado.productos.forEach(prod => {
                const rutaImg = prod.imagen ? "../" + prod.imagen : "../assets/img/default-prenda.png";
                const precio = parseFloat(prod.precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                const total = parseFloat(prod.total).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                const cantidad = prod.cantidad || 1;
                
                htmlDetalle += `
                    <div class="item-detalle" style="display: flex; gap: 15px; align-items: center; margin-bottom: 12px; border-bottom: 1px solid #eee; padding-bottom: 8px;">
                        <img src="${rutaImg}" alt="${prod.nombre}" class="item-imagen" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px;">
                        <div style="flex-grow: 1;">
                            <h4 class="nombre-product" style="margin: 0; font-size: 14px;">${prod.nombre}</h4>
                            <p class="product-details" style="margin: 2px 0; color: #666; font-size: 12px;">Unidades: ${cantidad}</p>
                            <p class="product-details" style="margin: 2px 0; color: #666; font-size: 12px;">Precio Unitario: $${precio}</p>
                        </div>
                        <div style="font-weight: bold; color: #5d2b90; font-size: 13px;">
                            Subtotal: $${total}
                        </div>
                    </div>
                `;
            });
        } else {
            htmlDetalle = "<p style='text-align:center; color:#666;'>No se encontraron artículos ni especificaciones registrados en este pedido.</p>";
        }

        if (contenedorLista) contenedorLista.innerHTML = htmlDetalle;
             
        const capaSombreado = document.querySelector(".sombreado");
        if (capaSombreado) capaSombreado.classList.add("aparecerSombreado");
    }
});

// 🚀 ESCUCHADOR DE CLICK PARA ACEPTAR Y EMPEZAR PASARELA DE COMPRA
document.addEventListener("click", async (e) => {
    if (e.target.matches(".btn-cotizacion--aceptar")) {
        const boton = e.target;
        
        // 🔄 CORRECCIÓN: Capturamos 'idCotizacion' desde el dataset específico
        const idCotizacion = boton.dataset.idCotizacion; 
        const precio = boton.dataset.precio;

        const capaSombreado = document.querySelector(".sombreado");
        if (capaSombreado) capaSombreado.classList.add("aparecerSombreado");

        await llamarComponente("#compraCarrito", "../componentesWeb/FormulairoCompra.html");

        // 🔍 DIAGNÓSTICO: Confirmar en la consola del navegador qué ID se va a enviar
        console.log("✈️ Pasando a RealizarCompra -> ID Cotización:", idCotizacion, "Precio:", precio);

        RealizarCompra("A Medida", {
            idCotizacion: parseInt(idCotizacion),
            precio: precio
        });

        const btnCancelar = document.querySelector("#cancelarCompra");
        if (btnCancelar) {
            btnCancelar.addEventListener("click", () => {
                document.querySelector("#compraCarrito").innerHTML = ""; 
                if (capaSombreado) capaSombreado.classList.remove("aparecerSombreado"); 
            });
        }
    }
});

