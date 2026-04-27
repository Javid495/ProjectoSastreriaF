const formulario = document.querySelector(".form");
const email = document.querySelector(".form--email");
const usuario = document.querySelector(".form--usuario");
const contra = document.querySelector(".form--contra");
const telefono = document.querySelector(".form--tel");

formulario.addEventListener("submit", (e) => {

    e.preventDefault();

    // Se preparan los datos para ser enviados

    const enviar = new URLSearchParams();

    enviar.append("correo", email.value);
    enviar.append("user", usuario.value);
    enviar.append("contra", contra.value);
    enviar.append("tel", telefono.value);


    fetch (`Prueba`, {
        method: `POST`,
        body: enviar
    })

    .then(response => response.text())
    .then(respuesta => {

        if (respuesta == "ok"){
            console.log("SE han enviado los datos a la base de datos");
            this.reset()
        }
    })

    .catch(error => console.error("No se enviaron los datos"));

})

