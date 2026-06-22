import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js";

document.addEventListener("DOMContentLoaded", async () => {
    await aparecerCont("../");
    
    // 1. VALIDACIÓN: Comprobar sesión activa
    const sesionActiva = await comprobarSesion("../");

    const noSessionState = document.getElementById("no-session-state");
    const userProfileContent = document.getElementById("user-profile-content");

    if (!sesionActiva) {
        userProfileContent.classList.add("profile-card--hidden");
        noSessionState.classList.remove("profile-card--hidden");
    } else {
        userProfileContent.classList.remove("profile-card--hidden");
        noSessionState.classList.add("profile-card--hidden");
        
        // Inicializamos toda la lógica interactiva y traemos los datos reales del servidor
        inicializarPerfil();
    }
});

function inicializarPerfil() {
    // --- ELEMENTOS DEL DOM ---
    const btnConfig = document.getElementById("btn-config");
    const btnEditAvatar = document.getElementById("btn-edit-avatar");
    const btnCancel = document.getElementById("btn-cancel");
    const formProfile = document.getElementById("profile-edit-state");
    const formErrorMsg = document.getElementById("form-error-msg");
    
    const viewState = document.getElementById("profile-view-state");
    const editState = document.getElementById("profile-edit-state");
    
    const viewUsername = document.getElementById("view-username");
    const viewPhone = document.getElementById("view-phone");
    const viewEmail = document.getElementById("view-email");
    
    const inputUsername = document.getElementById("input-username");
    const inputPhone = document.getElementById("input-phone");
    const inputEmail = document.getElementById("input-email");

    const avatarModal = document.getElementById("avatar-modal");
    const avatarOptions = document.querySelectorAll(".avatar-option:not(.avatar-option--upload)");
    const btnSelectAvatar = document.getElementById("btn-select-avatar");
    const userAvatar = document.getElementById("user-avatar");

    let avatarSeleccionadoRuta = "";
    let avatarParaGuardar = ""; // 🌟 Almacenará la ruta limpia (sin "../") lista para ir a la BD

    // --- CARGAR DATOS DESDE EL SERVLET ---
    fetch("../PerfilUsuario")
        .then(res => {
            if (!res.ok) throw new Error("No se pudo obtener la información del perfil.");
            return res.json();
        })
        .then(perfil => {
            viewUsername.textContent = perfil.nombre || "Sin nombre";
            viewPhone.textContent = perfil.telefono || "XXXXXXXXXX";
            viewEmail.textContent = perfil.correo || "Sin correo";

            // Guardamos el estado inicial que viene del servidor
            avatarParaGuardar = perfil.imagenAvatar || "images/Perfil/Ellipse 14.png";

            if (perfil.imagenAvatar && perfil.imagenAvatar.trim() !== "") {
                userAvatar.src = perfil.imagenAvatar.startsWith("images") ? "../" + perfil.imagenAvatar : perfil.imagenAvatar;
            }

            pintarProductosRecientes(perfil.productosRecientes);
            pintarHistorialPedidos(perfil.historialPedidos);
        })
        .catch(err => {
            console.error("Error al cargar los datos del perfil:", err);
            mostrarError("Error al sincronizar los datos con el servidor.");
        });


    // --- FUNCIONES DE RENDERIZADO DINÁMICO ---
    
    function pintarProductosRecientes(productos) {
        const contenedor = document.getElementById("recent-products-container");
        contenedor.innerHTML = ""; 

        if (!productos || productos.length === 0) {
            contenedor.innerHTML = "<p>No has consultado prendas recientemente.</p>";
            return;
        }

        productos.forEach(prod => {
            const cardHTML = `
                <div class="mini-card" data-id="${prod.id}">
                    <div class="mini-card__img-container">
                        <img src="../${prod.img}" alt="${prod.nombre}">
                    </div>
                    <h4>${prod.nombre}</h4>
                    <p class="mini-card__price">Precio: ${prod.valor.toLocaleString('es-CO')}</p>
                    <button class="btn-detail">Ver detalles</button>
                </div>
            `;
            contenedor.innerHTML += cardHTML;
        });

        // 🌟 ADICIONAL SOLICITADO: Agregar redirección dinámica con el ID al dar click
        contenedor.querySelectorAll(".btn-detail").forEach(btn => {
            btn.addEventListener("click", (e) => {
                const card = e.target.closest(".mini-card");
                const idPrenda = card.dataset.id;
                // Al estar en perfil.html (dentro de vistas), llamamos directo a la descripción
                window.location.href = `DescripcionProducto.html?id=${idPrenda}`;
            });
        });
    }

    function pintarHistorialPedidos(pedidos) {
        const contenedor = document.getElementById("orders-container");
        contenedor.innerHTML = "";

        if (!pedidos || pedidos.length === 0) {
            contenedor.innerHTML = "<p>No registras compras en tu cuenta.</p>";
            return;
        }

        pedidos.forEach(pedido => {
            const orderHTML = `
                <div class="order-item" data-id="${pedido.idPedido}">
                    <div class="order-item__info">
                        <p class="order-item__status"><strong>[${pedido.TipoCompra}]</strong> ${pedido.EstadoPedido}</p>
                        <p class="order-item__date">Fecha: ${pedido.FechaInicio}</p>
                        <p class="order-item__date" style="font-size: 0.85rem; color: #666;">Dirección: ${pedido.Direccion}</p>
                    </div>
                </div>
            `;
            contenedor.innerHTML += orderHTML;
        });
    }


    // --- LÓGICA DE INTERCAMBIO DE VISTAS Y VALIDACIONES ---
    
    btnConfig.addEventListener("click", () => {
        inputUsername.value = viewUsername.textContent.trim();
        inputPhone.value = viewPhone.textContent.trim();
        inputEmail.value = viewEmail.textContent.trim();
        
        formErrorMsg.style.display = "none";
        viewState.classList.add("profile-card--hidden");
        editState.classList.remove("profile-card--hidden");
    });

    btnCancel.addEventListener("click", () => {
        editState.classList.add("profile-card--hidden");
        viewState.classList.remove("profile-card--hidden");
    });

    // Envío del Formulario con persistencia total en el Servidor
    // Envío del Formulario con persistencia total en el Servidor y Validaciones estrictas
    formProfile.addEventListener("submit", (e) => {
        e.preventDefault(); 

        const nombre = inputUsername.value.trim();
        const telefono = inputPhone.value.trim();
        const correo = inputEmail.value.trim();

        // 1. VALIDACIÓN: Campos vacíos (Reforzada)
        if (nombre === "" || telefono === "" || correo === "") {
            mostrarError("Error: Todos los campos son obligatorios y no pueden contener solo espacios.");
            return;
        }

        // 2. VALIDACIÓN: Teléfono (Exactamente 10 dígitos)
        const regexTelefono = /^\d{10}$/; 
        if (!regexTelefono.test(telefono)) {
            mostrarError("Error: El teléfono debe tener exactamente 10 dígitos numéricos.");
            return;
        }

        // 3. VALIDACIÓN: Estructura base del correo
        const regexCorreo Estructura = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/; 
        if (!regexCorreoEstructura.test(correo)) {
            mostrarError("Error: Por favor ingresa una estructura de correo válida (ejemplo@dominio.com).");
            return;
        }

        // 4. VALIDACIÓN: Proveedores y extensiones permitidas (Whitelisting)
        const dominioCorreo = correo.split("@")[1].toLowerCase();
        const dominiosPermitidos = [
            "gmail.com", 
            "outlook.com", 
            "hotmail.com", 
            "yahoo.com", 
            "icloud.com"
        ];

        if (!dominiosPermitidos.includes(dominioCorreo)) {
            mostrarError("Error: El dominio '" + dominioCorreo + "' no está permitido. Usa un proveedor válido (Gmail, Outlook, Hotmail, Yahoo o iCloud).");
            return;
        }

        // Si pasa todos los filtros, limpiamos errores y procedemos
        formErrorMsg.style.display = "none";

        const datosFormulario = new URLSearchParams();
        datosFormulario.append("nombre", nombre);
        datosFormulario.append("telefono", telefono);
        datosFormulario.append("correo", correo);
        datosFormulario.append("imagenAvatar", avatarParaGuardar); 

        fetch("../PerfilUsuario", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: datosFormulario
        })
        .then(res => {
            if (!res.ok) throw new Error("Error interno en el servidor al actualizar.");
            return res.json();
        })
        .then(respuesta => {
            if (respuesta.status === "success") {
                viewUsername.textContent = nombre;
                viewPhone.textContent = telefono;
                viewEmail.textContent = correo;

                editState.classList.add("profile-card--hidden");
                viewState.classList.remove("profile-card--hidden");
            } else {
                mostrarError(respuesta.message || "No se pudieron salvar las modificaciones.");
            }
        })
        .catch(err => {
            console.error("Error al procesar el envío:", err);
            mostrarError("Fallo de comunicación con el servidor. Inténtalo más tarde.");
        });
    });

    function mostrarError(mensaje) {
        formErrorMsg.textContent = mensaje;
        formErrorMsg.style.display = "block";
    }

    // --- MODAL DE AVATARES ---
    btnEditAvatar.addEventListener("click", () => {
        avatarModal.classList.remove("avatar-modal--hidden");
    });

    avatarModal.addEventListener("click", (e) => {
        if (e.target === avatarModal) avatarModal.classList.add("avatar-modal--hidden");
    });

    avatarOptions.forEach(option => {
        option.addEventListener("click", () => {
            avatarOptions.forEach(opt => opt.classList.remove("avatar-option--selected"));
            option.classList.add("avatar-option--selected");
            const img = option.querySelector("img");
            if (img) avatarSeleccionadoRuta = img.getAttribute("src");
        });
    });

    btnSelectAvatar.addEventListener("click", () => {
        if (avatarSeleccionadoRuta) {
            userAvatar.src = avatarSeleccionadoRuta;
            
            // 🌟 PROCESAMIENTO: Si la imagen seleccionada tiene '../images/...', 
            // le quitamos el '../' para que se guarde de forma estándar en tu base de datos.
            avatarParaGuardar = avatarSeleccionadoRuta.startsWith("../") 
                ? avatarSeleccionadoRuta.replace("../", "") 
                : avatarSeleccionadoRuta;

            // 📢 AVISO AL USUARIO: Informamos que el cambio aún es temporal
            alert("¡Icono seleccionado! Recuerda que para guardar permanentemente tu nueva foto de perfil, debes ingresar a las configuraciones (icono de engranaje ⚙️) y hacer clic en el botón 'Hecho'.");
        }
        avatarModal.classList.add("avatar-modal--hidden");
    });
}