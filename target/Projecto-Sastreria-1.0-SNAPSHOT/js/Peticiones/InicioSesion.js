const formulario = document.querySelector(".form");
const inputUsuario = document.querySelector("#Usuario");
const inputContra = document.querySelector("#Contrasena");


formulario.addEventListener("submit", async (e) => {

    e.preventDefault();

    const datos = new URLSearchParams();

    console.log(inputUsuario.value);
    console.log(inputContra.value);
    
    
    datos.append("txtUser", inputUsuario.value);
    datos.append("txtContra", inputContra.value);


    const respuesta = await fetch('Login', {

            method: 'POST',
            body: datos
        }
    )

    console.log(respuesta);
    
    const validacion = await respuesta.text();
    console.log(validacion.trim());
    

    if (validacion.trim() === 'Hecho'){
        alert("Ingreso  con exito");
        window.location.href = 'index.html';
    }

    else{

        alert("El usuario ingresado no se encuentra registrado");
    }

})
