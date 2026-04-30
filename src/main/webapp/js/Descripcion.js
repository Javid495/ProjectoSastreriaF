const btnComentarios = document.querySelector(".producto--comentarios");
const btnCerrar = document.querySelector(".button__closed");

const ventEmergent = document.querySelector(".section__comments");
const sombreado = document.querySelector(".sombreado");


btnComentarios.addEventListener("click", (e) => {
    
    e.preventDefault();

    sombreado.classList.add("cuerpo--opaco");

    ventEmergent.classList.add("mostrarResena")
    
})

btnCerrar.addEventListener("click", (e) => {

    e.preventDefault();

    sombreado.classList.remove("cuerpo--opaco");

    ventEmergent.classList.remove("mostrarResena");
})