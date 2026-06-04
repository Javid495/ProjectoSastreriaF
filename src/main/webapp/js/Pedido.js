import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { llamarComponente } from "../helpers/CompHtml.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";
import { RealizarCompra } from "../helpers/ProcesoCompra.js";

const BtnRealizarP = document.querySelector(".btn-RealizarPedido");
const contPedidos = document.querySelector("#contenedorPedidos");
const contFormulario = document.querySelector("#MostraPedido");
const cuerpoPagina = document.querySelector("body");
const filtroEstadoContainer = document.querySelector("#filtro-estado-container");

// 🌐 Definimos urlBase arriba de manera global para evitar ReferenceError en las funciones
const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

/**
 * Cambiar entre Pedidos de Catálogo (Pendientes) y Pedidos Personalizados (Cotizados)
 * Se adjunta al objeto window para mantener la compatibilidad con los 'onclick' de tu HTML
 */
window.cambiarTipoPedido = function(tipo, elemento) {
    // 1. Gestionar estados visuales de las pestañas
    document.querySelectorAll(".tab-tipo").forEach(btn => btn.classList.remove("active"));
    elemento.classList.add("active");

    // 2. Limpiar el contenedor principal antes de inyectar los nuevos datos
    contPedidos.innerHTML = "";

    // 3. Evaluar el tipo de pestaña seleccionada
    if (tipo === 'catalogo') {
        if (filtroEstadoContainer) filtroEstadoContainer.style.display = "block"; // Mostrar filtro de estados
        MostrarPedidosUser();
    } else if (tipo === 'medida') {
        if (filtroEstadoContainer) filtroEstadoContainer.style.display = "none";  // Ocultar filtro de estados
        cargarMisCotizaciones();
    }
}

// 📦 VISTA 1: Obtener Pedidos Estándar (Tabla pedidos de catálogo)
function MostrarPedidosUser() {
    fetch(`${urlBase}/ObtenerPedidos`)
    .then(response => response.json())
    .then(data => {
        
        if(!data.logeado){
            contPedidos.innerHTML = `
                <h1>Se requiere un inicio de sesion html</h1>
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
                <button class="card__button" data-id = "${pedido.id}" data-tipo="${pedido.tipo}">Ver detalles</button>
            </article> `;
        });
    })
    .catch(error => console.error("Error con la conexion de los pedidos:", error));
}

// ✨ VISTA 2: Obtener Respuestas de Diseños desde Cero (Tabla de Cotizaciones)
function cargarMisCotizaciones() {
    fetch(`${urlBase}/MisCotizaciones`)
        .then(res => {
            if (!res.ok) throw new Error("No autorizado o error de servidor");
            return res.json();
        })
        .then(cotizaciones => {

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
                            <button class="btn-cotizacion btn-cotizacion--rechazar">Rechazar</button>
                            <button class="btn-cotizacion btn-cotizacion--aceptar" data-id="${c.id}" data-precio="${c.precio}">Aceptar y Pagar</button>
                        </div>
                    </article>
                `;
            });
        })
        .catch(err => {
            console.error("Error cargando cotizaciones:", err);
        });
}

// ⏳ Inicialización base de la página
document.addEventListener("DOMContentLoaded", async () =>{
    await aparecerCont("../");
    comprobarSesion("../");

    // Por defecto renderiza la pestaña seleccionada de fábrica ('catalogo')
    MostrarPedidosUser();
});

// 📋 BOTÓN PRINCIPAL: Abrir formulario e interceptar Submit
BtnRealizarP.addEventListener("click", async () =>{
    await llamarComponente("#MostraPedido" , "../componentesWeb/FormularioPedidos.html");
    
    document.addEventListener("submit", async (evento) => {
        if (evento.target.matches("#form-solicitud-personalizada")) {
            evento.preventDefault();
            
            const formulario = evento.target;
            const formData = new FormData();
    
            // 1. Recolectamos los campos de texto simples
            formData.append("tipoPrenda", document.querySelector("#tipo-prenda").value);
            formData.append("telas", document.querySelector("#sugerencia-telas").value);
            formData.append("talla", document.querySelector("#talla-prenda").value);
            formData.append("descripcion", document.querySelector("#desc-pedido").value);
    
            // 2. Procesamos los 3 inputs oscuros de las medidas y los unimos
            const inputsMedidas = document.querySelectorAll(".medidas-inputs .input-dark");
            const arrayMedidas = [];
            inputsMedidas.forEach(input => {
                if(input.value) arrayMedidas.push(input.value);
            });
            // Lo guardamos separado por comas: "85,62,90"
            formData.append("medidas", arrayMedidas.join(","));
    
            // 3. Capturamos el archivo de imagen adjunto
            const inputFile = document.querySelector("#file-upload");
            if (inputFile.files.length > 0) {
                formData.append("fotoReferencia", inputFile.files[0]);
            }
    
            // 4. Despachamos la información al Backend usando Fetch
            fetch(`${urlBase}/RegistrarPedidoMedida`, {
                method: "POST",
                body: formData // ⚠️ Al enviar un FormData, NO debes configurar headers de Content-Type manualmente
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("¡Tu solicitud de diseño ha sido enviada con éxito! El administrador la cotizará pronto.");
                    document.querySelector("#MostraPedido").innerHTML = ""; // Cerramos el modal
                    if(typeof MostrarPedidosUser === 'function') MostrarPedidosUser(); // Recargamos lista si existe
                } else {
                    alert("Error al procesar la solicitud: " + data.mensaje);
                }
            })
            .catch(err => console.error("Error en el envío del diseño a medida:", err));
        }
    });
});

// ❌ INTERFAZ MODAL: Controladores para vaciar/cerrar modales abiertos
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

// TARJETAS DE PEDIDOS: Apertura y desglose dinámico de artículos por ID
contPedidos.addEventListener("click", async (evento) => {
    const botonDetalles = evento.target.closest(".card__button");
    
    if (botonDetalles) {
        // 🌟 Capturamos tanto el ID como el Tipo desde el botón
        const idPedido = botonDetalles.dataset.id;
        const tipoPedido = botonDetalles.dataset.tipo || "Catálogo";

        // 1. Cargamos el componente visual de la ventana emergente
        await llamarComponente("#MostraPedido", "../componentesWeb/mostrarDetallesPedido.html");

        // 2. Solicitamos los artículos usando las variables correctas y urlBase dinámico
        fetch(`${urlBase}/ObtenerDetallePedido?idPedido=${idPedido}&tipoPedido=${tipoPedido}`)
            .then(response => response.json())
            .then(productos => {
                const contenedorLista = document.querySelector("#listaArticulosPedido");
                let htmlDetalle = "";

                if (productos.length === 0) {
                    htmlDetalle = "<p style='text-align:center; color:#666;'>No se encontraron productos para este pedido.</p>";
                } else {
                    productos.forEach(prod => {
                        const rutaImg = prod.imagen ? "../" + prod.imagen : "../assets/img/default-prenda.png";
                        const precio = parseFloat(prod.precio).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                        const total = parseFloat(prod.totalLineal).toLocaleString('es-CO', { minimumFractionDigits: 0 });
                        const cantidad = prod.cantidad || 1;
                        
                        htmlDetalle += `
                            <div class="item-detalle">
                                <img src="${rutaImg}" alt="${prod.nombre}" class="item-imagen">
                                <div style="flex-grow: 1;">
                                    <h4 class="nombre-product">${prod.nombre}</h4>
                                    <p class= "product-details">Unidades: ${cantidad}</p>
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
            
        cuerpoPagina.classList.add("overlay");
    }
});

// Escucha global de eventos dentro de la pantalla "Mis Pedidos"
document.addEventListener("click", async (e) => {
    
    // Detectamos si el usuario presionó el botón de aceptar una cotización oficial
    if (e.target.matches(".btn-cotizacion--aceptar")) {
        const boton = e.target;
        
        // 1. Extraemos las propiedades de la cotización específica
        const idCotizacion = boton.dataset.id;
        const precio = boton.dataset.precio;

        // 2. Activamos el sombreado oscuro de fondo
        const capaSombreado = document.querySelector(".sombreado");
        if (capaSombreado) capaSombreado.classList.add("aparecerSombreado");

        // 3. Inyectamos de forma asíncrona el formulario de pago reutilizable
        await llamarComponente("#compraCarrito", "../componentesWeb/FormulairoCompra.html");

        // 4. Inicializamos la lógica del formulario pasándole los parámetros "A Medida"
        RealizarCompra("A Medida", {
            idCotizacion: idCotizacion,
            precio: precio
        });

        // 5. [Opcional] Añadir soporte al botón Cancelar dentro del modal recién creado
        const btnCancelar = document.querySelector("#cancelarCompra");
        if (btnCancelar) {
            btnCancelar.addEventListener("click", () => {
                document.querySelector("#compraCarrito").innerHTML = ""; // Desmota el formulario
                capaSombreado.classList.remove("aparecerSombreado"); // Quita el fondo oscuro
            });
        }
    }
});

