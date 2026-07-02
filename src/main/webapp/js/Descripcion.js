import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { CargarDetallesProd } from "../helpers/CargarDetallesProd.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";

const btnComentarios = document.querySelector(".producto--comentarios");
const btnCerrar = document.querySelector(".button__closed");
const ventEmergent = document.querySelector(".section__comments");
const sombreado = document.querySelector(".sombreado");

const btnPublicar = document.querySelector(".btn--publicar");
const cajaComentario = document.querySelector("#cajaComentario");

// 🌟 NUEVOS ELEMENTOS PARA LA IMAGEN
const btnSeleccionarImagen = document.querySelector("#btnSeleccionarImagen");
const inputImagen = document.querySelector("#inputImagen");
const nombreArchivo = document.querySelector("#nombreArchivo");

let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
let productoId = null;

document.addEventListener("DOMContentLoaded", async (e) => {
    await aparecerCont("../");
    comprobarSesion("../");

    const url = new URLSearchParams(window.location.search);
    productoId = url.get("id");

    if (productoId) {
        CargarDetallesProd(productoId);
        cargarResenasPrenda(productoId); 
    }
});

// 🌟 EVENTO: Al darle clic al botón estético, simulamos el clic en el input file oculto
if (btnSeleccionarImagen && inputImagen) {
    btnSeleccionarImagen.addEventListener("click", () => inputImagen.click());

    // Muestra el nombre de la imagen en pantalla cuando el usuario la selecciona
    inputImagen.addEventListener("change", () => {
        if (inputImagen.files.length > 0) {
            nombreArchivo.textContent = inputImagen.files[0].name;
        } else {
            nombreArchivo.textContent = "";
        }
    });
}

// Función para descargar y pintar las reseñas desde el Servlet
function cargarResenasPrenda(prendaId) {
    const contenedor = document.querySelector(".resenas");
    if (!contenedor) return;

    fetch(`${urlBase}/ResenasController?prendaId=${prendaId}`)
        .then(res => res.json())
        .then(resenas => {
            contenedor.innerHTML = ""; 

            if (resenas.length === 0) {
                contenedor.innerHTML = `<p style="color: gray; font-style: italic;">Esta prenda aún no tiene reseñas. ¡Sé el primero en calificarla!</p>`;
                return;
            }

            resenas.forEach(r => {
                const card = document.createElement("article");
                card.className = "resenas__card";
                
                // 🌟 CORRECCIÓN AQUÍ: Usamos 'imagenResena' que es el nombre que genera Gson
                // Además, validamos que no sea la imagen por defecto para que no sature la interfaz
                let htmlImagen = "";
                if (r.imagenResena && r.imagenResena !== "" && r.imagenResena !== "images/Resenas/default.png") {
                    htmlImagen = `<img src="${urlBase}/${r.imagenResena}" alt="Imagen de reseña" class="resenas__img" style="max-width: 150px; display: block; margin-top: 10px; border-radius: 5px;">`;
                }

                card.innerHTML = `
                    <h3 class="resenas__user">${r.nombreUsuario}</h3>
                    <p class="resenas__comentario">${r.descripcion}</p>
                    ${htmlImagen}
                `;
                contenedor.appendChild(card);
            });
        })
        .catch(err => console.error("Error cargando reseñas:", err));
}

// Evento para enviar el comentario y la imagen al Servlet
btnPublicar.addEventListener("click", (e) => {
    e.preventDefault();
    
    const textoComentario = cajaComentario.value.trim();

    // 🛡️ VALIDACIÓN DE CLIENTE ENREJADA: No permite enviar si el texto está vacío
    // Da igual si seleccionaron una foto; sin texto la reseña es inválida.
    if (!textoComentario || textoComentario === "") {
        alert("Por favor, escribe un comentario descriptivo. La opinión de texto es obligatoria.");
        cajaComentario.focus();
        return; // 🚏 Frena el envío inmediatamente
    }

    // Usamos FormData para empaquetar de forma nativa datos mixtos (texto + binarios)
    const formData = new FormData();
    formData.append("prendaId", productoId);
    formData.append("comentario", textoComentario);

    // Si el usuario seleccionó una imagen (es opcional), la añadimos al envío
    if (inputImagen && inputImagen.files.length > 0) {
        formData.append("imagen", inputImagen.files[0]); 
    }

    fetch(`${urlBase}/ResenasController`, {
        method: "POST",
        // Recordatorio: NO agregues headers de Content-Type aquí, deja que FormData calcule su boundary.
        body: formData
    })
    .then(async res => {
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Fallo al publicar");
        return data;
    })
    .then(data => {
        alert(data.message);
        
        // Limpieza de campos al finalizar con éxito
        cajaComentario.value = ""; 
        if (inputImagen) inputImagen.value = ""; 
        if (nombreArchivo) nombreArchivo.textContent = ""; 
        
        sombreado.classList.remove("cuerpo--opaco");
        ventEmergent.classList.remove("mostrarResena");
        
        // Recargar las opiniones actualizadas de la prenda
        cargarResenasPrenda(productoId);
    })
    .catch(err => {
        console.error("Error al procesar la reseña:", err);
        alert(err.message); 
    });
});

// Eventos de apertura y cierre
btnComentarios.addEventListener("click", (e) => {
    e.preventDefault();
    sombreado.classList.add("cuerpo--opaco");
    ventEmergent.classList.add("mostrarResena");
});

btnCerrar.addEventListener("click", (e) => {
    e.preventDefault();
    sombreado.classList.remove("cuerpo--opaco");
    ventEmergent.classList.remove("mostrarResena");
});