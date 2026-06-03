export function cerrarSesionServidor() {
    const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));

    // Enviamos una petición al servlet de logout (lo crearemos a continuación)
    fetch(`${urlBase}/CerrarSesion`, { method: 'POST' })
    .then(response => {
        if (response.ok) {
            console.log("Sesión destruida correctamente.");
            // Redireccionamos al index o recargamos la página para que vuelva a mostrar el botón de ingreso
            window.location.href = `${urlBase}/index.html`;
        }
    })
    .catch(error => console.error("Error al cerrar sesión:", error));
}