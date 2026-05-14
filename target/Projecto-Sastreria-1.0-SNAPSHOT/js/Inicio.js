import { aparecerCont } from "../helpers/RelizarPeticion.js"; 

document.addEventListener("DOMContentLoaded", async (e) =>{

    e.preventDefault();

    await aparecerCont("./");

})