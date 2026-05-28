import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { CargarDetallesAdmin } from "../helpers/CargarDetallesAdmin.js";


const ContAside = document.querySelector(".sidebar");

document.addEventListener("DOMContentLoaded", (e) => {

    MostrarSide();
    
    const url = new URLSearchParams(window.location.search);

    const PrendaId = url.get("id");

    if (PrendaId){

        CargarDetallesAdmin(PrendaId);
    }
})

