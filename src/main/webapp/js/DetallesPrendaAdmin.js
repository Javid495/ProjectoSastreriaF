import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { CargarDetallesAdmin } from "../helpers/CargarDetallesAdmin.js";
import { ModificarPrendas } from "../helpers/ModificarPrendasAdmin.js";
import { PintarFilaVariante } from "../helpers/AñadirModiPrendas.js"; 
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js"

const ContAside = document.querySelector(".sidebar");

document.addEventListener("DOMContentLoaded", (e) => {

    MostrarSide();
    
    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });

    const url = new URLSearchParams(window.location.search);
    const PrendaId = url.get("id");

    if (PrendaId){
        CargarDetallesAdmin(PrendaId);
        ModificarPrendas(PrendaId);
        // ✨ Llama a la nueva función pasándole el ID de la prenda
        cargarResenasAdmin(PrendaId);
    }

    // --- Escuchador para Añadir Nuevas Filas de Tallaje (Tu lógica intacta) ---
    const btnAgregarVariante = document.querySelector("#btnAgregarVariante");
    const contenedorVariantes = document.querySelector("#contenedorVariantes");

    if (btnAgregarVariante && contenedorVariantes) {
        btnAgregarVariante.addEventListener("click", (e) => {
            e.preventDefault();
            PintarFilaVariante(contenedorVariantes, null, "", 0, 0);
        });
    }

    // --- LÓGICA DE IMÁGENES (Tu código original intacto) ---
    const btnAgregarImg = document.querySelector("#btnSubirFotos") || document.querySelector(".imagenes-galeria-container .btn-subir-imagenes"); 
    const inputOculto = document.querySelector("#inputArchivoOculto");
    const contenedorImgs = document.querySelector("#contenedorImgs"); 

    if (btnAgregarImg && inputOculto) {
        btnAgregarImg.addEventListener("click", (e) => {
            e.preventDefault(); 
            inputOculto.click(); 
        });

        inputOculto.addEventListener("change", (e) => {
            const archivos = e.target.files;
            if (archivos.length > 0) {
                Array.from(archivos).forEach(archivo => {
                    const lector = new FileReader();
                    lector.onload = function(eventoLector) {
                        const rutaBase64 = eventoLector.target.result;
                        const divMiniatura = document.createElement("div");
                        divMiniatura.classList.add("img-miniatura-admin");

                        const nuevaImg = document.createElement("img");
                        nuevaImg.setAttribute("src", rutaBase64);
                        nuevaImg.fileObject = archivo; 
                        nuevaImg.setAttribute("data-nuevo", "true"); 

                        const botonEliminar = document.createElement("span");
                        botonEliminar.innerHTML = "×";
                        botonEliminar.classList.add("btn-eliminar-foto"); 
                
                        botonEliminar.addEventListener("click", () => {
                            divMiniatura.remove();
                        });

                        divMiniatura.appendChild(nuevaImg);
                        divMiniatura.appendChild(botonEliminar);
                        contenedorImgs.appendChild(divMiniatura);
                    };
                    lector.readAsDataURL(archivo);
                });
            }
            inputOculto.value = ""; 
        });
    }
});

// =========================================================================
// ✨ FUNCIÓN NUEVA: Obtener y renderizar las reseñas en el panel de Admin
// =========================================================================
function cargarResenasAdmin(prendaId) {
    const contenedor = document.querySelector("#contenedorResenasAdmin");
    if (!contenedor) return;

    // Hacemos el GET al Servlet mapeado como /ResenasController
    fetch(`../ResenasController?prendaId=${prendaId}`)
        .then(response => {
            if (!response.ok) throw new Error("Error en la petición de reseñas");
            return response.json();
        })
        .then(listaResenas => {
            contenedor.innerHTML = ""; // Limpiamos el texto de 'Cargando...'

            // Escenario A: No hay reseñas registradas aún
            if (listaResenas.length === 0) {
                contenedor.innerHTML = `<p style="color: #777; font-style: italic; background: #fdfdfd; padding: 15px; border-left: 4px solid #ccc;">Esta prenda aún no cuenta con reseñas de clientes.</p>`;
                return;
            }

            // Escenario B: Iterar e imprimir cada reseña que devolvió Gson
            listaResenas.forEach(resena => {
                const tarjeta = document.createElement("div");
                tarjeta.className = "tarjeta-resena-admin";
                
                // Estilos rápidos en línea (puedes pasarlos a tu ModificarPrendas.css)
                tarjeta.style.cssText = "display: flex; gap: 20px; background: #fdfdfd; border: 1px solid #e2e8f0; padding: 15px; margin-bottom: 15px; border-radius: 8px; align-items: flex-start; box-shadow: 0 2px 4px rgba(0,0,0,0.02);";

                // Validamos avatar del cliente (si no tiene, colocamos uno genérico de tu proyecto)
                const rutaAvatar = resena.avatarUsuario ? `../${resena.avatarUsuario}` : "../images/default-avatar.png";

                // Evaluamos si el cliente subió una imagen personalizada a su comentario
                let bloqueImagenAdjunta = "";
                if (resena.imagenResena && !resena.imagenResena.includes("default.png")) {
                    bloqueImagenAdjunta = `
                        <div style="margin-top: 10px;">
                            <a href="../${resena.imagenResena}" target="_blank">
                                <img src="../${resena.imagenResena}" style="max-width: 150px; max-height: 150px; border-radius: 6px; border: 1px solid #cbd5e1; object-fit: cover;" alt="Evidencia de cliente">
                            </a>
                        </div>`;
                }

                tarjeta.innerHTML = `
                    <img src="${rutaAvatar}" style="width: 50px; height: 50px; border-radius: 50%; object-fit: cover; border: 2px solid #cbd5e1; flex-shrink: 0;" alt="Usuario">
                    <div style="flex-grow: 1;">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <strong style="font-size: 16px; color: #1e293b;">${resena.nombreUsuario}</strong>
                            <small style="color: #94a3b8; font-size: 12px;">ID Reseña: #${resena.id}</small>
                        </div>
                        <p style="margin: 6px 0 0 0; color: #475569; font-size: 14.5px; line-height: 1.5; white-space: pre-line;">
                            ${resena.descripcion}
                        </p>
                        ${bloqueImagenAdjunta}
                    </div>
                `;

                contenedor.appendChild(tarjeta);
            });
        })
        .catch(error => {
            console.error("Error al cargar reseñas en admin:", error);
            contenedor.innerHTML = `<p style="color: #ef4444; font-weight: bold;">⚠️ Hubo un inconveniente al cargar las opiniones del producto.</p>`;
        });
}

