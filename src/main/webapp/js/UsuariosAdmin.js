import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";

let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
let usuariosLocales = []; 

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Inyectamos la sidebar compartida
    await MostrarSide();
    
    // 2. Cargamos los usuarios desde el nuevo Servlet real
    solicitarUsuariosServidor();

    // 3. Listener en tiempo real de la barra de búsqueda
    const buscador = document.querySelector("#input-buscar-usuario");
    if (buscador) {
        buscador.addEventListener("input", (e) => {
            const criterio = e.target.value.toLowerCase().trim();
            filtrarYRenderizarUsuarios(criterio);
        });
    }

    // 4. Listener global para cierre de sesión
    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });
});

function solicitarUsuariosServidor() {
    // Apuntamos al nuevo servlet dedicado del administrador
    fetch(`${urlBase}/ListarUsuariosAdmin`)
        .then(res => {
            if (!res.ok) throw new Error("Error en la respuesta del servidor");
            return res.json();
        })
        .then(data => {
            usuariosLocales = data;
            filtrarYRenderizarUsuarios(""); // Renderizado inicial sin filtros
        })
        .catch(err => console.error("Error cargando usuarios desde la BD:", err));
}

function filtrarYRenderizarUsuarios(filtro) {
    const tablero = document.querySelector("#tablero-usuarios");
    if (!tablero) return;

    tablero.innerHTML = "";

    // Filtramos localmente sobre el array en memoria
    const filtrados = usuariosLocales.filter(u => {
        const nom = u.nombre ? u.nombre.toLowerCase() : "";
        const email = u.email ? u.email.toLowerCase() : "";
        const tel = u.telefono ? u.telefono.toString() : "";
        return nom.includes(filtro) || email.includes(filtro) || tel.includes(filtro);
    });

    if (filtrados.length === 0) {
        tablero.innerHTML = `<p style="text-align:center; padding: 40px; color: var(--color-gris-oscuro);">No hay usuarios registrados que coincidan con los parámetros.</p>`;
        return;
    }

    // Fragmento en memoria para inyectar todo de un solo golpe al DOM
    const fragmento = document.createDocumentFragment();

    filtrados.forEach(u => {
        const tarjeta = document.createElement("article");
        tarjeta.className = "card-usuario";

        const rutaAvatar = u.avatar ? `${urlBase}/${u.avatar}` : `${urlBase}/images/Perfil/Ellipse 14.png`;

        tarjeta.innerHTML = `
            <div class="usuario-perfil-meta">
                <img src="${rutaAvatar}" class="usuario-avatar-circular" alt="Avatar">
                <div class="usuario-datos">
                    <h3>${u.nombre || "Sin nombre registrado"}</h3>
                    <p><strong>Email:</strong> ${u.email || "No disponible"}</p>
                    <p><strong>Teléfono:</strong> ${u.telefono || "No disponible"}</p>
                </div>
            </div>
            <button type="button" class="btn-ver-mas" data-id="${u.id}">Ver más</button>
        `;

        // Evento click al botón para redireccionar pasando el ID por parámetro query string
        tarjeta.querySelector(".btn-ver-mas").addEventListener("click", () => {
            window.location.href = `DetallesUsuario.html?idUsuario=${u.id}`;
        });

        fragmento.appendChild(tarjeta);
    });

    // Inserción única y eficiente
    tablero.appendChild(fragmento);
}