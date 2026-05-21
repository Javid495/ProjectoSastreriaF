import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { CargarDetallesProd } from "../helpers/CargarDetallesProd.js"

const btnComentarios = document.querySelector(".producto--comentarios");
const btnCerrar = document.querySelector(".button__closed");

const ventEmergent = document.querySelector(".section__comments");
const sombreado = document.querySelector(".sombreado");


document.addEventListener("DOMContentLoaded", async (e) =>{

    await aparecerCont("../");

    //Guardamos la url de la busqueda
    const url = new URLSearchParams(window.location.search);
    
    console.log(url);
    // de la url tomamos el id que hace referencia al producto
    const ProductoId = url.get("id");

    console.log(ProductoId);
    
    
    if (ProductoId){
        CargarDetallesProd(ProductoId);
    }

})

//Evento del boton comentarios
btnComentarios.addEventListener("click", (e) => {
    
    e.preventDefault();

    sombreado.classList.add("cuerpo--opaco");

    ventEmergent.classList.add("mostrarResena")
    
})

//Evento del boton realizar comentario
btnCerrar.addEventListener("click", (e) => {

    e.preventDefault();

    sombreado.classList.remove("cuerpo--opaco");

    ventEmergent.classList.remove("mostrarResena");
})