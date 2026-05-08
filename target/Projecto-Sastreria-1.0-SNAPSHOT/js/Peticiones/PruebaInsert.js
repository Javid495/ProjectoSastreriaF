// Tomamos los elementos del formulario del DOM
const formulario = document.querySelector(".form");
const email = document.querySelector(".form--email");
const usuario = document.querySelector(".form--usuario");
const contra = document.querySelector(".form--contra");
const telefono = document.querySelector(".form--tel");

// Añadimos un evento de tipo submit al contenedor de formulario
formulario.addEventListener("submit", (e) => {

    // Prevenimos cualquier recarga de la pagina
    e.preventDefault();

    // Se preparan los datos para ser enviados
    const enviar = new URLSearchParams();

    //Se manda los datos del registro al objeto enviar
    enviar.append("correo", email.value);
    enviar.append("user", usuario.value);
    enviar.append("contra", contra.value);
    enviar.append("tel", telefono.value);

    // Se envia los datos al servelt de registro
    fetch (`Registro`, {
        method: `POST`,
        body: enviar
    })

    // Segun la respuesta que retorna validamos
    .then(response => response.text())
    .then(respuesta => {

        if (respuesta == "ok"){
            console.log("Se han enviado los datos a la base de datos");
            this.reset()
        }
    })
    
    .catch(error => console.error("No se enviaron los datos"));

    alert("Se ha registrado correctamente");
    formulario.reset();

})

