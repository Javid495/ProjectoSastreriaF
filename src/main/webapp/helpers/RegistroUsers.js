// Tomamos los elementos del formulario del DOM
const formulario = document.querySelector(".form");
const email = document.querySelector(".form--email");
const usuario = document.querySelector(".form--usuario");
const contra = document.querySelector(".form--contra");
const telefono = document.querySelector(".form--tel");

// 🌟 FUNCIÓN DE RESPALDO UI/UX: Crea alertas tipo "Toast" elegantes y dinámicas
function mostrarAlerta(mensaje, tipo = "error") {
    // 1. Eliminar alertas previas flotantes u overlays para evitar acumulaciones
    const alertaExistente = document.querySelector(".alerta-overlay");
    if (alertaExistente) alertaExistente.remove();

    // 2. Crear el contenedor de fondo (Backdrop) que bloquea y oscurece la pantalla
    const overlay = document.createElement("div");
    overlay.className = "alerta-overlay";

    // 3. Crear la estructura de la tarjeta de alerta centradita
    const caja = document.createElement("div");
    caja.className = `alerta-caja alerta-${tipo}`;

    const icono = tipo === "success" ? "✅" : "⚠️";

    caja.innerHTML = `
        <div class="alerta-icono">${icono}</div>
        <div class="alerta-mensaje">${mensaje}</div>
        <button class="alerta-boton-cerrar">Entendido</button>
    `;

    // 4. Armar e inyectar en el DOM
    overlay.appendChild(caja);
    document.body.appendChild(overlay);

    // 5. EVENTO DE CIERRE: Al darle click al botón "Entendido"
    const botonCerrar = caja.querySelector(".alerta-boton-cerrar");
    botonCerrar.addEventListener("click", () => {
        overlay.classList.add("alerta-fade-out");
        setTimeout(() => overlay.remove(), 250); // Remueve del DOM tras la animación
    });

    // 6. RESPALDO: Cierre automático a los 5 segundos si el usuario no le da click
    setTimeout(() => {
        if (document.body.contains(overlay)) {
            overlay.classList.add("alerta-fade-out");
            setTimeout(() => overlay.remove(), 250);
        }
    }, 5000);
}

// Añadimos el evento submit manejado con Async/Await
formulario.addEventListener("submit", async (e) => {
    e.preventDefault();

    // 1. Sanitización de datos (Quitar espacios en blanco al inicio y final)
    const valorEmail = email.value.trim();
    const valorUsuario = usuario.value.trim();
    const valorContra = contra.value.trim();
    const valorTelefono = telefono.value.trim();

    // 2. VALIDACIÓN: Campos vacíos
    if (!valorEmail || !valorUsuario || !valorContra || !valorTelefono) {
        mostrarAlerta("⚠️ Todos los campos son obligatorios. No se permiten registros vacíos.", "error");
        return;
    }

    // 3. VALIDACIÓN: Nombre (Solo letras y espacios, ideal para nombres/apellidos españoles con tildes y Ñ)
    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;
    if (!regexNombre.test(valorUsuario)) {
        mostrarAlerta("👤 El nombre de usuario solo debe contener letras.", "error");
        return;
    }

    // 4. VALIDACIÓN: Correo con extensión/proveedor válido (Gmail, Outlook, Hotmail, Yahoo, iCloud, etc.)
    const regexEmail = /^[a-zA-Z0-9._%+-]+@(gmail|outlook|hotmail|yahoo|icloud|live)\.[a-zA-Z]{2,}(\.[a-zA-Z]{2,})?$/i;
    if (!regexEmail.test(valorEmail)) {
        mostrarAlerta("📧 Por favor, ingresa un correo electrónico válido (ej: usuario@gmail.com).", "error");
        return;
    }

    // 5. VALIDACIÓN: Teléfono (Solo números y exactamente 10 dígitos)
    const regexTelefono = /^\d{10}$/;
    if (!regexTelefono.test(valorTelefono)) {
        mostrarAlerta("📱 El número de teléfono debe tener exactamente 10 dígitos numéricos.", "error");
        return;
    }

    // 6. PROCESAMIENTO: Si pasa todas las validaciones, preparamos el envío
    const enviar = new URLSearchParams();
    enviar.append("correo", valorEmail);
    enviar.append("user", valorUsuario);
    enviar.append("contra", valorContra);
    enviar.append("tel", valorTelefono);

    try {
        // Mostramos un estado de carga sutil en el botón (Opcional UX)
        const boton = formulario.querySelector("button[type='submit']");
        if(boton) boton.disabled = true;

        const response = await fetch("Registro", {
            method: "POST",
            body: enviar
        });

        if (!response.ok) throw new Error("Error en la respuesta de red");

        const respuestaServidor = await response.text();

        // 7. FLUJO ASÍNCRONO CORRECTO: Validamos la respuesta real del Servlet
        if (respuestaServidor.trim() === "ok") {
            mostrarAlerta("✅ ¡Registro completado con éxito!", "success");
            formulario.reset(); // Ahora sí limpia de forma segura
        } else {
            mostrarAlerta(`❌ Error en el registro: ${respuestaServidor}`, "error");
        }

        if(boton) boton.disabled = false;

    } catch (error) {
        console.error("No se enviaron los datos:", error);
        mostrarAlerta("📡 Error de conexión con el servidor. Inténtalo más tarde.", "error");
        const boton = formulario.querySelector("button[type='submit']");
        if(boton) boton.disabled = false;
    }
});

