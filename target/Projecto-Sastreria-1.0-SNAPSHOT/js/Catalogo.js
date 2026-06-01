import { crearCards } from "../helpers/Cards.js";
import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js"; 

const contenedor = document.querySelector(".popular__cards");

async function cargarCatalogo(){

    try {
        // Obtenemos la lista de productos del catalogo
        const respuesta = await fetch("../ObtenerPrendas");
    
        //verificamos que los datos hayan llegado correctamente 
        if(!respuesta.ok) throw new Error("Error en la obtencion de datos");

        //Recibimos el archivo.json 
        const prendas = await respuesta.json();

        contenedor.innerHTML = "";

        //Recorremos los productos
        prendas.forEach(prenda => {
            //Llamamos la funcion crearCards y vamos creando nuestras cards
            const nuevaCard = crearCards(prenda);
            
            contenedor.appendChild(nuevaCard);
        });
    }

    //En caso de un error en la creacion de las cards mostramos el error
    catch (error){

        console.error("Hubo algun error en la carga del catalogo: ", error);

        contenedor.innerHTML = "<p>No se cargaron las prendas correctamente por favor vuelva a intentarlo mas adelante</p>";
    }
}

document.addEventListener("DOMContentLoaded", async (e) =>{

    e.preventDefault();
    
    await aparecerCont("../");
    comprobarSesion("../");
    cargarCatalogo();

});