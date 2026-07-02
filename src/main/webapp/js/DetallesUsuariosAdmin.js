import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";

let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

document.addEventListener("DOMContentLoaded", async () => {
    // Carga la barra lateral de la plataforma
    await MostrarSide();

    // Capturamos el id enviado en la URL (?idUsuario=X) desde usuariosAdmin.html
    const parametrosURL = new URLSearchParams(window.location.search);
    const idUsuario = parametrosURL.get("idUsuario");

    if (idUsuario) {
        cargarFichaCompletaUsuario(idUsuario);
    } else {
        alert("Error: No se detectó un identificador de usuario válido.");
        window.location.href = "UsuariosAdmin.html";
    }

    // Evento para cerrar sesión
    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });
});

// Petición al nuevo Servlet encargado de los detalles para el Admin
function cargarFichaCompletaUsuario(id) {
    fetch(`${urlBase}/DetallesUsuarioAdmin?idUsuario=${id}`)
        .then(res => {
            if (!res.ok) throw new Error("No se pudo obtener la información del servidor.");
            return res.json();
        })
        .then(data => {
            // El servlet nos devuelve un mapa con { usuario: {...}, historial: [...] }
            construirPerfilIzquierdo(data.usuario);
            construirHistorialDerecho(data.historial);
        })
        .catch(err => {
            console.error("Error al procesar el expediente del usuario:", err);
            const contenedorHistorial = document.querySelector("#contenedor-historial");
            if (contenedorHistorial) {
                contenedorHistorial.innerHTML = `<p style="color:red; text-align:center; padding:20px;">Error al cargar el expediente.</p>`;
            }
        });
}

// Dibuja los datos básicos del usuario a la izquierda
function construirPerfilIzquierdo(usuario) {
    const columnaIzquierda = document.querySelector("#perfil-usuario-card");
    if (!columnaIzquierda || !usuario) return;

    // Controlamos los nombres de las propiedades que vienen del objeto IniciarSesion vía Gson
    const nombreUsuario = usuario.usuario || "Usuario Anónimo";
    const correoUsuario = usuario.email || "Sin correo registrado";
    const telefonoUsuario = usuario.telefono || "Sin registrar";
    
    // Si manejas imágenes/avatares en tu tabla Usuarios, se usa, si no, va la por defecto
    const avatarSrc = usuario.imagen ? `${urlBase}/${usuario.imagen}` : `${urlBase}/images/Perfil/Ellipse 14.png`;

    columnaIzquierda.innerHTML = `
        <div class="avatar-wrapper-detalles">
            <img src="${avatarSrc}" alt="Avatar de Cliente">
        </div>
        <h2>${nombreUsuario}</h2>
        <div class="info-items-admin">
            <p><strong>Correo Electrónico:</strong><br>${correoUsuario}</p>
            <p><strong>Teléfono Móvil:</strong><br>${telefonoUsuario}</p>
            <p><strong>Rol en Sistema:</strong><br>Cliente Regular</p>
        </div>
    `;
}

// Dibuja las acciones/logs de navegación a la derecha
function construirHistorialDerecho(historial) {
    const contenedorHistorial = document.querySelector("#contenedor-historial");
    if (!contenedorHistorial) return;

    contenedorHistorial.innerHTML = "";

    if (!historial || historial.length === 0) {
        contenedorHistorial.innerHTML = `<p style="text-align:center; padding:40px; color: var(--color-gris-oscuro);">El usuario no registra acciones recientes en el sistema.</p>`;
        return;
    }

    // Fragmento en memoria para no saturar el DOM con cada vuelta del bucle
    const fragmento = document.createDocumentFragment();

    historial.forEach(log => {
        const filaLog = document.createElement("div");
        filaLog.className = "card-evento-historial";

        // Mapea directamente con las propiedades limpias que genera tu HistorialUsuarioDAO
        filaLog.innerHTML = `
            <div class="evento-descripcion">
                <span>${log.descripcion || "Acción registrada en el sistema"}</span>
            </div>
            <div class="evento-timestamp">
                <div>📅 ${log.fecha}</div>
                <div style="margin-top: 2px; font-weight: bold;">🕒 ${log.hora}</div>
            </div>
        `;
        fragmento.appendChild(filaLog);
    });

    contenedorHistorial.appendChild(fragmento);
}