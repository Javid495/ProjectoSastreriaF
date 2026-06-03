import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";

let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

// Comprueba el estado actual en la base de datos
function verificarPedidosPorCotizar() {
    fetch(`${urlBase}/AdminCotizaciones?accion=contar`)
        .then(res => res.json())
        .then(data => {
            const barra = document.querySelector(".cotizaciones-bar");
            // Eliminamos elementos previos si existen
            const badgePrevio = barra.querySelector(".badge-contador");
            if(badgePrevio) badgePrevio.remove();

            if (data.cantidad > 0) {
                barra.classList.add("tiene-pendientes");
                
                // Inyectamos el indicador numérico llamativo
                const badge = document.createElement("span");
                badge.className = "badge-contador";
                badge.innerText = `${data.cantidad} NUEVOS`;
                barra.insertBefore(badge, barra.querySelector(".btn-play"));
            } else {
                barra.classList.remove("tiene-pendientes");
            }
        });
}

// Crea y despliega la card emergente (Modal) sobre la vista general
function abrirModalCotizaciones() {
    fetch(`${urlBase}/AdminCotizaciones?accion=listar`)
        .then(res => res.json())
        .then(pedidos => {
            // Creamos el contenedor del modal superpuesto
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
                            <input type="number" placeholder="Precio Cotizado ($)" required style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 1;" class="input-precio">
                            <label style = "font-size:10px;">Fecha Aproximada de entrega:</label>
                            <input type="date" required 
                                style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 1; min-width: 140px;" 
                                class="input-fecha">
                            <input type="text" placeholder="Comentario o validez de fecha" required style="padding: 6px; border: 1px solid #ccc; border-radius: 4px; flex: 2;" class="input-comentario">
                            <button type="submit" style="background: #5d2b90; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer;">Enviar</button>
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
                    <div class="lista-por-cotizar">
                        ${tarjetasHTML || '<p style="text-align:center;">No hay elementos que procesar.</p>'}
                    </div>
                </div>
            `;

            document.body.appendChild(overlay);
        });
}

document.addEventListener("DOMContentLoaded", () => {
    MostrarSide();
    verificarPedidosPorCotizar();
    
    // Escuchamos el clic en la barra superior de cotizaciones
    const barraCotizar = document.querySelector(".cotizaciones-bar");
    barraCotizar.addEventListener("click", () => {
        if (barraCotizar.classList.contains("tiene-pendientes")) {
            abrirModalCotizaciones();
        }
    });

    //Metodo para cerrar sesion desde admin
        const btnCerrar = document.querySelector("#cerrarSesion");
        
        btnCerrar.addEventListener("click", (e) =>{
            cerrarSesionServidor();
        })
});


