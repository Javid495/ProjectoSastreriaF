// llamo a los elementos del DOM
const formulario = document.querySelector(".form");
const inputUsuario = document.querySelector("#Usuario");
const inputContra = document.querySelector("#Contrasena");


// Añadimos el evento submit al formulario de inicio de sesion
formulario.addEventListener("submit", async (e) => {

    // Prevemos cualquier recarga involuntaria de la pagina
    e.preventDefault();

    // Creamos una variable para mandar a java
    const datos = new URLSearchParams();

    //añadimos los datos de inicio sesion a la variable datos
    datos.append("txtUser", inputUsuario.value);
    datos.append("txtContra", inputContra.value);

    //Iniciamos un fetch de tipo post añadiendo la variable datos
    // y nos comunicamos con el servelt de inicio sesion
    const respuesta = await fetch('Login', {
            method: 'POST',
            body: datos
        }
    )
    
    // Esperamos la respuesta de java
    const validacion = await respuesta.text();
    console.log(validacion);
    

    // Segun la respuesta que reciba valida si hay inicio de sesion
    if (validacion.trim() === 'Hecho'){
        alert("Ingreso  con exito");
        window.location.href = 'index.html';
    }

    else{
        alert("El usuario ingresado no se encuentra registrado");
    }

})
