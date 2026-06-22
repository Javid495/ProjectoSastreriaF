import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";

let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
let pedidosLocales = []; // Almacén en memoria para filtrar sin sobrecargar la red
let estadoActivo = "elaboracion"; // Estado por defecto

// 📦 Carga los datos desde el servidor y dispara el primer renderizado
function cargarTableroPedidos() {
    fetch(`${urlBase}/AdminPedidosController`)
        .then(res => res.json())
        .then(data => {
            pedidosLocales = data;
            renderizarPedidos();
        })
        .catch(err => console.error("Error al devengar pedidos de la BD:", err));
}

// Dibuja las Cards dentro de '#tablero-pedidos' aplicando el filtro seleccionado
function renderizarPedidos() {
    const tablero = document.querySelector("#tablero-pedidos");
    if (!tablero) return;

    tablero.innerHTML = ""; // Limpiamos las tarjetas estáticas

    // FILTRADO ROBUSTO: Incluye los estados principales y el nuevo estado "entregado"
    const pedidosFiltrados = pedidosLocales.filter(p => {
        const est = p.estado.toLowerCase().trim();
        
        if (estadoActivo === "pendientes" || estadoActivo === "pendiente") {
            return est.includes("pendiente");
        }
        if (estadoActivo === "elaboracion") {
            return est.includes("elaboracion");
        }
        // 🔥 CORREGIDO: Añadimos && !est.includes("entregado") para que no se mezclen
        if (estadoActivo === "entregar") {
            return (est.includes("entregar") || est.includes("entrega")) && !est.includes("entregado");
        }
        if (estadoActivo === "entregado") {
            return est.includes("entregado");
        }
        return est === estadoActivo;
    });

    if (pedidosFiltrados.length === 0) {
        tablero.innerHTML = `<p style="grid-column: 1/-1; text-align: center; padding: 40px; color: #666;">
                                No hay pedidos en estado "${estadoActivo}" en este momento.
                             </p>`;
        return;
    }

    pedidosFiltrados.forEach(p => {
        const article = document.createElement("article");
        article.className = "card-pedido";

        // Evalúa si el pedido es personalizado "A Medida"
        const esAMedida = p.medidas && !p.medidas.includes("N/A");

        // Estructura de marcas de selección para el combo select de estados
        const esPendiente = p.estado.includes("pendiente") ? "selected" : "";
        const esElaboracion = p.estado.includes("elaboracion") ? "selected" : "";
        const esEntregar = (p.estado.includes("entregar") || p.estado.includes("entrega")) && !p.estado.includes("entregado") ? "selected" : "";
        const esEntregado = p.estado.includes("entregado") ? "selected" : "";

        // 🎚️ Inyección de contenido dinámico según las reglas de negocio solicitadas
        let cuerpoTarjetaHTML = "";

        if (esAMedida) {
            // 🧵 CASO A MEDIDA: Muestra Usuario, Nombre de la prenda, Medidas y los agregados recomendados
            cuerpoTarjetaHTML = `
                <h2 class="pedido-cliente">Pedido #${p.id} (A Medida)</h2>
                <p class="pedido-meta"><strong>Cliente:</strong> ${p.usuario || "No asignado"}</p>
                <p class="pedido-meta"><strong>Prenda:</strong> ${p.prenda || "Diseño Personalizado"}</p>
                <p class="pedido-meta"><strong>Medidas:</strong> ${p.medidas}</p>
                <p class="pedido-meta"><strong>Monto Venta:</strong> $${p.total}</p>
            `;
        } else {
            // 📦 CASO CATÁLOGO: Muestra Usuario, Fecha de realización y los agregados recomendados
            cuerpoTarjetaHTML = `
                <h2 class="pedido-cliente">Pedido #${p.id} (Catálogo)</h2>
                <p class="pedido-meta"><strong>Cliente:</strong> ${p.usuario || "No asignado"}</p>
                <p class="pedido-meta"><strong>Fecha Realizado:</strong> ${p.fecha}</p>
                <p class="pedido-meta"><strong>Monto Venta:</strong> $${p.total}</p>
            `;
        }

        // Bloque común final para todas las tarjetas (Selector de estado y Botón de detalles)
        article.innerHTML = `
            ${cuerpoTarjetaHTML}
            
            <div class="pedido-estado-container" style="margin-top: 10px;">
                <label>Estado:</label>
                <select class="select-estado-pedido" data-id="${p.id}">
                    <option value="pendiente" ${esPendiente}>Pendiente</option>
                    <option value="elaboracion" ${esElaboracion}>En elaboración</option>
                    <option value="entregar" ${esEntregar}>Por entregar</option>
                    <option value="entregado" ${esEntregado}>Entregado</option> 
                </select>
            </div>
            
            <button type="button" class="btn-detalles-pedido" data-id="${p.id}" style="margin-top: 10px;">Ver Detalles</button>
        `;

        tablero.appendChild(article);
    });
}

// 🔀 Intercambiador de pestañas expuesto a 'window'
window.cambiarFiltroEstado = function(estado, botonElemento) {
    estadoActivo = estado.toLowerCase();
    document.querySelectorAll(".tab-estado").forEach(btn => btn.classList.remove("active"));
    botonElemento.classList.add("active");
    renderizarPedidos();
};

// 🔄 Modificar el estado directamente en la base de datos al cambiar el combo select
document.addEventListener("change", (e) => {
    if (e.target.matches(".select-estado-pedido")) {
        const idPedido = e.target.dataset.id;
        const nuevoEstado = e.target.value;

        const params = new URLSearchParams();
        params.append("idPedido", idPedido);
        params.append("estado", nuevoEstado);

        fetch(`${urlBase}/AdminPedidosController`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params
        })
        .then(res => res.json())
        .then(res => {
            if (res.success) {
                alert(`¡Pedido #${idPedido} movido a "${nuevoEstado}" con éxito!`);
                cargarTableroPedidos(); 
            } else {
                alert("No se pudo actualizar el estado en el servidor: " + (res.mensaje || "Error"));
            }
        })
        .catch(err => console.error("Error actualizando estado:", err));
    }
});

// 📊 Mantiene la funcionalidad de animación si existen cotizaciones por procesar
function verificarPedidosPorCotizar() {
    fetch(`${urlBase}/AdminCotizaciones?accion=contar`)
        .then(res => res.json())
        .then(data => {
            const barra = document.querySelector(".cotizaciones-bar");
            if (!barra) return;

            const badgePrevio = barra.querySelector(".badge-contador");
            if(badgePrevio) badgePrevio.remove();

            if (data.cantidad > 0) {
                // Inyecta la clase CSS encargada de ejecutar tu animación
                barra.classList.add("tiene-pendientes");
                
                const badge = document.createElement("span");
                badge.className = "badge-contador";
                badge.innerText = `${data.cantidad} NUEVOS`;
                barra.insertBefore(badge, barra.querySelector(".btn-play"));
            } else {
                barra.classList.remove("tiene-pendientes");
            }
        })
        .catch(err => console.error("Error verificando conteo de cotizaciones:", err));
}

// 🪟 Despliega el modal emergente de cotizaciones pendientes
function abrirModalCotizaciones() {
    // 🗓️ Obtenemos la fecha de hoy en formato local (Colombia) para restringir el input date
    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd = String(hoy.getDate()).padStart(2, '0');
    const fechaMinima = `${yyyy}-${mm}-${dd}`; // Resultado: "2026-06-21"

    fetch(`${urlBase}/AdminCotizaciones?accion=listar`)
        .then(res => res.json())
        .then(pedidos => {
            const overlay = document.createElement("div");
            overlay.className = "modal-admin-overlay";
            overlay.id = "modal-cotizar-emergente";

            let tarjetasHTML = "";
            pedidos.forEach(p => {
                const foto = p.imagen ? `${urlBase}/${p.imagen}` : `${urlBase}/images/Perfil/Ellipse 14.png`;
                tarjetasHTML += `
                    <div style="border: 1px solid #ddd; padding: 15px; margin-bottom: 15px; border-radius: 8px; background: #fafafa;">
                        <p><strong>Cliente:</strong> ${p.email}</p>
                        <p><strong>Prenda:</strong> ${p.tipo} | <strong>Tela sugerida:</strong> ${p.tela}</p>
                        <p><strong>Medidas:</strong> ${p.medidas}</p>
                        <p><strong>Detalles:</strong> ${p.descripcion}</p>
                        <p><br>Sugerencia Estampado:</p>
                        ${p.imagen ? `<img src="${foto}" style="width:80px; height:80px; object-fit:cover; margin: 8px 0; border-radius:4px;">` : ''}
                        
                        <form class="form-enviar-cotizacion" data-id="${p.id}" style="margin-top: 12px; display: flex; gap: 10px; flex-wrap: wrap;">
                            <input type="number" placeholder="Precio Cotizado ($)" min="5000" required class="input-precio" style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 1;">
                            
                            <label style="font-size:10px; display: flex; align-items: center;">Fecha Entrega:</label>
                            
                            <input type="date" min="${fechaMinima}" required class="input-fecha" style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 1; min-width: 140px;">
                            
                            <input type="text" placeholder="Comentario" required class="input-comentario" style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 2;">
                            <button type="submit" style="background: #5d2b90; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-weight: bold;">Enviar</button>
                        </form>
                    </div>
                `;
            });

            overlay.innerHTML = `
                <div class="modal-admin-box">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:15px;">
                        <h3 style="margin:0; color:#333;">Pedidos en Espera de Cotización</h3>
                        <button onclick="document.querySelector('#modal-cotizar-emergente').remove()" style="background:none; border:none; font-size:20px; cursor:pointer;">&times;</button>
                    </div>
                    <div class="lista-por-cotizar">${tarjetasHTML || '<p style="text-align:center; padding: 20px;">No hay elementos que procesar.</p>'}</div>
                </div>
            `;
            document.body.appendChild(overlay);
        });
}

// ⏳ Inicialización del ciclo de vida del documento
document.addEventListener("DOMContentLoaded", async () => {
    await MostrarSide();
    await verificarPedidosPorCotizar();
    cargarTableroPedidos(); 
    
    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });

    const barraCotizar = document.querySelector(".cotizaciones-bar");
    if (barraCotizar) {
        barraCotizar.addEventListener("click", () => {
            if (barraCotizar.classList.contains("tiene-pendientes")) {
                abrirModalCotizaciones();
            }
        });
    }
});

// Escucha global para el envío de formularios de cotización con validaciones robustas
document.addEventListener("submit", (evento) => {
    if (evento.target.matches(".form-enviar-cotizacion")) {
        evento.preventDefault();
        const formulario = evento.target;
        
        const idPedidoMedida = formulario.dataset.id;
        const precioRaw = formulario.querySelector(".input-precio").value;
        const fechaLimite = formulario.querySelector(".input-fecha").value;
        const comentario = formulario.querySelector(".input-comentario").value;

        // 🛑 1. Validación de campos vacíos obligatorios
        if (!precioRaw.trim() || !fechaLimite.trim()) {
            alert("⚠️ Error: El precio y la fecha de entrega son campos totalmente obligatorios.");
            return;
        }

        // 🛑 2. Validación de valor mínimo real para confección en Colombia ($5.000 COP)
        const precioValor = parseFloat(precioRaw);
        if (isNaN(precioValor) || precioValor < 5000) {
            alert("⚠️ Error: El precio de la cotización debe ser un número válido y comenzar desde los $5,000 pesos colombianos mínimos de mano de obra.");
            return;
        }

        // 🛑 3. Validación estricta de fecha para evitar días pasados
        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0); // Limpiamos horas para comparar únicamente las fechas reales

        // Dividimos la fecha del input (YYYY-MM-DD) para construir el objeto Date local sin desfases UTC
        const [anio, mes, dia] = fechaLimite.split("-").map(Number);
        const fechaSeleccionada = new Date(anio, mes - 1, dia); // Enero es 0 en JavaScript

        if (fechaSeleccionada < hoy) {
            alert("❌ Error crítico: No puedes asignar una fecha de entrega anterior al día de hoy.");
            return;
        }

        // Si pasa todas las pruebas, preparamos el envío
        const datosCuerpo = new URLSearchParams();
        datosCuerpo.append("idPedidoMedida", idPedidoMedida);
        datosCuerpo.append("precio", precioValor);
        datosCuerpo.append("fechaLimite", fechaLimite);
        datosCuerpo.append("comentario", comentario);

        fetch(`${urlBase}/GuardarCotizacion`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: datosCuerpo
        })
        .then(respuesta => respuesta.json())
        .then(resultado => {
            if (resultado.success) {
                alert("¡Cotización guardada exitosamente!");
                const tarjetaContenedora = formulario.closest("div");
                const listaContenedora = tarjetaContenedora.parentElement;
                tarjetaContenedora.remove();

                if (listaContenedora.children.length === 0) {
                    listaContenedora.innerHTML = '<p style="text-align:center; padding: 20px;">No hay elementos que procesar.</p>';
                }
                verificarPedidosPorCotizar();
                cargarTableroPedidos(); 
            } else {
                alert("Error al guardar: " + resultado.mensaje);
            }
        })
        .catch(error => console.error("Error procesando cotización:", error));
    }
});

// 👁️ Escucha el botón "Ver Detalles" para desplegar modales personalizados
document.addEventListener("click", (e) => {
    if (e.target.matches(".btn-detalles-pedido")) {
        const idPedido = e.target.dataset.id;

        fetch(`${urlBase}/AdminPedidosDetalles?idPedido=${idPedido}`)
            .then(res => res.json())
            .then(data => {
                abrirModalDetalleEspecifico(data, idPedido);
            })
            .catch(err => console.error("Error cargando detalles:", err));
    }
});

function abrirModalDetalleEspecifico(data, idPedido) {
    const modalExistente = document.querySelector(".modal-overlay");
    if (modalExistente) modalExistente.remove();

    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "modal-detalle-pedido";

    let contenidoInterno = "";

    if (data.tipo === "A Medida" || (data.medidas && !data.medidas.includes("N/A"))) {
        const arrayMedidas = data.medidas ? data.medidas.split(/[,\-]/) : ["0","0","0"];
        const m1 = arrayMedidas[0] ? arrayMedidas[0].trim() : 0;
        const m2 = arrayMedidas[1] ? arrayMedidas[1].trim() : 0;
        const m3 = arrayMedidas[2] ? arrayMedidas[2].trim() : 0;
        
        const fotoPrenda = data.imagen ? `${urlBase}/${data.imagen}` : `${urlBase}/images/Perfil/Ellipse 14.png`;

        contenidoInterno = `
            <div class="modal-container">
                <div class="modal-header">
                    <button type="button" class="btn-back" onclick="document.querySelector('#modal-detalle-pedido').remove()">
                        <img src="../images/DetallesProducto/Vector.png" alt="Volver">
                    </button>
                    <h2>Descripción del pedido hecho a Medida #${idPedido}</h2>
                </div>
                <form id="form-solicitud-personalizada" onsubmit="event.preventDefault();">
                    <div class="form-grid">
                        <div class="form-group">
                            <label>Tipo de prenda:</label>
                            <input type="text" value="${data.prenda || 'No especificada'}" class="input-grey" readonly>
                        </div>
                        <div class="form-group">
                            <label>Sugerencia de telas / Elegida:</label>
                            <input type="text" value="${data.tela || 'No especificada'}" class="input-grey" readonly>
                        </div>
                    </div>
                    <div class="inline-row">
                        <div class="form-group form-group--row">
                            <label>Cliente:</label>
                            <input type="text" value="${data.email}" class="input-grey" style="width:180px;" readonly>
                        </div>
                        <div class="form-group form-group--row medidas-inputs">
                            <label>Medidas:</label>
                            <input type="number" value="${m1}" class="input-dark" readonly>
                            <input type="number" value="${m2}" class="input-dark" readonly>
                            <input type="number" value="${m3}" class="input-dark" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Descripción del pedido:</label>
                        <textarea class="textarea-full" readonly>${data.descripcion || 'Sin descripción'}</textarea>
                    </div>
                    <div class="form-group">
                        <label>Imagen de referencia asignada:</label>
                        <div class="upload-zone zoom-container">
                            <img src="${fotoPrenda}" class="img-zoom-efecto" alt="Prenda referencia">
                        </div>
                    </div>
                    <div class="modal-footer" style="justify-content: space-between; align-items: center;">
                        <h3 style="font-family: var(--tipo-letra); font-size: 24px;">Precio Final Cotizado: $${data.total}</h3>
                        <button type="button" class="btn-footer btn-cancelar" onclick="document.querySelector('#modal-detalle-pedido').remove()">Cerrar</button>
                    </div>
                </form>
            </div>`;
    } else {
        let tarjetasPrendasHTML = "";
        data.prendas.forEach(p => {
            const pathImg = p.imagen.startsWith("http") || p.imagen.startsWith("images") ? `${urlBase}/${p.imagen}` : `${urlBase}/${p.imagen}`;
            tarjetasPrendasHTML += `
                <div class="card-prenda-detalle" style="background: var(--color-FondoInputs); border-radius: var(--radio-card); padding: 15px; display: flex; gap: 15px; align-items: center;">
                    <div class="zoom-container" style="width: 80px; height: 80px; overflow:hidden; border-radius: 8px;">
                        <img src="${pathImg}" class="img-zoom-efecto" style="width: 100%; height: 100%; object-fit: cover;">
                    </div>
                    <div>
                        <h4 style="margin:0; color: var(--color-Tipografia);">${p.nombre}</h4>
                        <p style="margin: 4px 0; font-size: 14px; color: var(--color-gris-oscuro);">Talla: <strong>${p.talla}</strong> | Cantidad: <strong>${p.cantidad || 1}</strong></p>
                        <p style="margin:0; font-weight: bold; color: var(--color-letra);">$${p.subtotal || p.precio}</p>
                    </div>
                </div>`;
        });

        contenidoInterno = `
            <div class="modal-container" style="max-width: 750px;">
                <div class="modal-header">
                    <button type="button" class="btn-back" onclick="document.querySelector('#modal-detalle-pedido').remove()">
                        <img src="../images/DetallesProducto/Vector.png" alt="Volver">
                    </button>
                    <h2>Información del pedido de Catálogo #${idPedido}</h2>
                </div>
                <p style="padding: 0 2rem; margin-bottom: 10px;"><strong>Comprador:</strong> ${data.email} | <strong>Fecha Compra:</strong> ${data.fecha}</p>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; padding: var(--padding-seccion); max-height: 380px; overflow-y: auto;">
                    ${tarjetasPrendasHTML}
                </div>
                <div class="modal-footer" style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--color-gris-claro); padding-top: 15px;">
                    <h2 style="font-family: var(--tipo-letra); margin-left: 2rem;">Total Compra: $${data.total}</h2>
                    <button type="button" class="btn-footer btn-confirmar" onclick="document.querySelector('#modal-detalle-pedido').remove()" style="background: var(--color-gris-oscuro);">Hecho</button>
                </div>
            </div>`;
    }

    overlay.innerHTML = contenidoInterno;
    document.body.appendChild(overlay);
}
