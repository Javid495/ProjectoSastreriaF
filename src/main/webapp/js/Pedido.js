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
let fotosReferenciaArr = []; // 📸 Almacén temporal para las imágenes añadidas

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

// 🚀 ESCUCHADOR DE SUBMIT: Registro de solicitudes personalizadas con validaciones estrictas
document.addEventListener("submit", async (evento) => {
    if (evento.target.matches("#form-solicitud-personalizada")) {
        evento.preventDefault();
        
        const formulario = evento.target;

        // 1. CAPTURA DE VALORES Y ELIMINACIÓN DE ESPACIOS
        const tipoPrenda = document.querySelector("#tipo-prenda").value.trim();
        const telas = document.querySelector("#sugerencia-telas").value.trim();
        const talla = document.querySelector("#talla-prenda").value.trim();
        const descripcion = document.querySelector("#desc-pedido").value.trim();

        // 2. VALIDACIÓN: Campos Generales Vacíos
        if (tipoPrenda === "" || telas === "" || talla === "" || descripcion === "") {
            alert("❌ Todos los campos principales (Tipo de prenda, Telas, Categoría/Talla y Descripción) son obligatorios.");
            return;
        }

        // 3. VALIDACIÓN: Mínimo de caracteres para Tipo de Prenda
        if (tipoPrenda.length < 5) {
            alert("❌ El tipo de prenda es demasiado corto. Debe tener al menos 5 caracteres.");
            return;
        }

        // 4. VALIDACIÓN: Mínimo de caracteres para la Tela (Igual que el tipo de prenda)
        if (telas.length < 5) {
            alert("❌ El campo de tela/material debe tener al menos 5 caracteres.");
            return;
        }

        // 5. VALIDACIÓN: No saltarse la categoría/talla (Control de placeholders por defecto)
        if (talla.toLowerCase() === "seleccionar" || talla === "0" || talla === "") {
            alert("❌ Por favor, selecciona una categoría o talla válida de la lista.");
            return;
        }

        // 6. VALIDACIÓN: Descripción con letras reales
        const regexLetras = /[a-zA-ZáéíóúÁÉÍÓÚñÑ]/;
        if (!regexLetras.test(descripcion)) {
            alert("❌ La descripción no es válida. Debe contener letras explicativas sobre el diseño.");
            return;
        }

        // 7. VALIDACIÓN: Medidas numéricas estrictas entre 30 y 200
        const inputsMedidas = document.querySelectorAll(".medidas-inputs .input-dark");
        const arrayMedidas = [];
        let medidasValidas = true;

        if (inputsMedidas.length === 0) {
            alert("❌ No se encontraron campos de medidas configurados en el formulario.");
            return;
        }

        for (let input of inputsMedidas) {
            const valorMedida = input.value.trim();
            
            // Verificar que no se envíen vacías
            if (valorMedida === "") {
                alert("❌ Todas las casillas de medidas son obligatorias.");
                medidasValidas = false;
                break;
            }

            const numero = Number(valorMedida);

            // Verificar que sea un número real y esté en el rango de 30 a 200
            if (isNaN(numero) || numero < 30 || numero > 200) {
                alert(`❌ Medida inválida (${valorMedida}). Recuerda que las medidas deben ser únicamente números enteros entre 30 y 200 cm.`);
                medidasValidas = false;
                break;
            }

            arrayMedidas.push(numero);
        }

        if (!medidasValidas) return; // Frena el envío si alguna medida falló

        // 8. VALIDACIÓN: Imagen de referencia obligatoria (Que no pase sin foto)
        // Filtramos las fotos reales que no sean 'null' dentro del almacén temporal
        const fotosReales = fotosReferenciaArr.filter(archivo => archivo !== null);
        
        if (fotosReales.length === 0) {
            alert("❌ La imagen de referencia es obligatoria. Por favor, sube al menos una foto o boceto de tu diseño.");
            return;
        }

        // --- SI PASA TODAS LAS VALIDACIONES, SE CREA EL FORMDATA Y SE ENVÍA ---
        const formData = new FormData();
        formData.append("tipoPrenda", tipoPrenda);
        formData.append("telas", telas);
        formData.append("talla", talla);
        formData.append("descripcion", descripcion);
        formData.append("medidas", arrayMedidas.join(","));

        // Adjuntamos las fotos validadas al FormData
        fotosReales.forEach(archivo => {
            formData.append("fotoReferencia", archivo); 
        });

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
                fotosReferenciaArr = []; // Vaciamos el array para el próximo pedido
            } else {
                alert("Error al procesar la solicitud: " + data.mensaje);
            }
        })
        .catch(err => console.error("Error en el envío del diseño a medida:", err));
    }
});

// 🖼️ ESCUCHADOR DE CAMBIO: Captura la imagen y genera la miniatura dinámicamente
document.addEventListener("change", (evento) => {
    if (evento.target.matches("#file-upload")) {
        const input = evento.target;
        
        if (input.files && input.files[0]) {
            const archivo = input.files[0];
            fotosReferenciaArr.push(archivo); // Guardamos en nuestro array global

            const uploadZone = input.closest(".upload-zone");
            const lector = new FileReader();

            lector.onload = function(e) {
                // Creamos el contenedor de la miniatura
                const divMiniatura = document.createElement("div");
                divMiniatura.className = "preview-thumb";
                // Guardamos el índice actual para saber cuál remover luego
                divMiniatura.dataset.index = fotosReferenciaArr.length - 1; 

                divMiniatura.innerHTML = `
                    <img src="${e.target.result}" alt="Vista previa">
                    <button type="button" class="btn-remove-thumb">&times;</button>
                `;

                // Lo insertamos en la zona de carga justo antes del botón (+)
                const plusBox = uploadZone.querySelector(".plus-box");
                uploadZone.insertBefore(divMiniatura, plusBox);
            };

            lector.readAsDataURL(archivo);
            
            // Limpiamos el valor del input para que permita volver a seleccionar la misma foto si se desea
            input.value = "";
        }
    }
});

// 🗑️ ESCUCHADOR PARA QUITAR FOTOS: Remueve la miniatura de la vista y del array
document.addEventListener("click", (evento) => {
    if (evento.target.matches(".btn-remove-thumb")) {
        const boton = evento.target;
        const miniatura = boton.closest(".preview-thumb");
        const indice = parseInt(miniatura.dataset.index);

        // Marcamos como null para no alterar los índices de los demás elementos visibles
        fotosReferenciaArr[indice] = null; 
        miniatura.remove();
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

