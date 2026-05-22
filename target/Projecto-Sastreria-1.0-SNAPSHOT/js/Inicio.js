import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { comprobarSesion } from "../helpers/ComprobarSesion.js"

document.addEventListener("DOMContentLoaded", async (e) =>{

    e.preventDefault();

    await aparecerCont("./");

    comprobarSesion();

})