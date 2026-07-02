// Llamo a los elementos del DOM
const formulario = document.querySelector(".form");
const inputUsuario = document.querySelector("#Usuario");
const inputContra = document.querySelector("#Contrasena");

// 🌟 REUSAMOS TU FUNCIÓN DE ALERTA MODAL (Para mantener la consistencia visual)
function mostrarAlerta(mensaje, tipo = "error") {
    const alertaExistente = document.querySelector(".alerta-overlay");
    if (alertaExistente) alertaExistente.remove();

    const overlay = document.createElement("div");
    overlay.className = "alerta-overlay";

    const caja = document.createElement("div");
    caja.className = `alerta-caja alerta-${tipo}`;
    const icono = tipo === "success" ? "✅" : "⚠️";

    caja.innerHTML = `
        <div class="alerta-icono">${icono}</div>
        <div class="alerta-mensaje">${mensaje}</div>
        <button class="alerta-boton-cerrar">Entendido</button>
    `;

    overlay.appendChild(caja);
    document.body.appendChild(overlay);

    const botonCerrar = caja.querySelector(".alerta-boton-cerrar");
    botonCerrar.addEventListener("click", () => {
        overlay.classList.add("alerta-fade-out");
        setTimeout(() => overlay.remove(), 250);
    });
}

// Añadimos el evento submit al formulario de inicio de sesión
formulario.addEventListener("submit", async (e) => {
    // Prevenimos cualquier recarga involuntaria de la página
    e.preventDefault();

    // 1. Sanitizar entradas (Remover espacios vacíos accidentales al inicio/final)
    const usuarioVal = inputUsuario.value.trim();
    const contraVal = inputContra.value.trim();

    // 2. VALIDACIÓN LOCAL: Evitar enviar campos vacíos al Servlet
    if (!usuarioVal || !contraVal) {
        mostrarAlerta("🔒 Por favor, ingresa tu usuario y contraseña.", "error");
        return;
    }

    // 3. UX: Desactivar el botón para evitar múltiples clics concurrentes
    const botonSubmit = formulario.querySelector("button[type='submit']");
    if (botonSubmit) {
        botonSubmit.disabled = true;
        botonSubmit.textContent = "Verificando...";
    }

    // 4. Creamos la variable de envío
    const datos = new URLSearchParams();
    datos.append("txtUser", usuarioVal);
    datos.append("txtContra", contraVal);

    try {
        // Iniciamos el fetch de tipo POST comunicándonos con el Servlet
        const respuesta = await fetch('Login', {
            method: 'POST',
            body: datos
        });

        // Validamos si la respuesta del servidor es correcta a nivel HTTP (200-299)
        if (!respuesta.ok) throw new Error("Error en la respuesta del servidor");

        // Esperamos la respuesta estructurada de Java
        const validacion = await respuesta.json();
        console.log("Respuesta del servidor de autenticación:", validacion);

        // 5. Según la respuesta que reciba valida si hay inicio de sesión exitoso
        if (validacion.status === 'Hecho') {
            mostrarAlerta("🎉 ¡Ingreso exitoso! Redireccionando...", "success");
            
            // Esperar un segundo breve para que el usuario logre leer la alerta de éxito
            setTimeout(() => {
                window.location.href = validacion.redireccion;
            }, 1200);

        } else {
            // Un tip de seguridad: Es mejor decir "Credenciales incorrectas" de forma general 
            // para no darle pistas a atacantes de qué nombres de usuario sí existen.
            mostrarAlerta("❌ El usuario o la contraseña son incorrectos.", "error");
            
            // Reactivamos el botón si las credenciales fallan para que pueda corregir
            if (botonSubmit) {
                botonSubmit.disabled = false;
                botonSubmit.textContent = "Ingresar"; // O el texto original que uses
            }
        }

    } 
    
    catch (error) {
        console.error("Error crítico en el proceso de Login:", error);
        mostrarAlerta("Nombre de usuario (Correo) o Contraseña incorrectos intentemos nuevamente ", "error");
        
        // Reactivamos el botón en caso de error de red para permitir reintentos
        if (botonSubmit) {
            botonSubmit.disabled = false;
            botonSubmit.textContent = "Ingresar";
        }
    }
});
