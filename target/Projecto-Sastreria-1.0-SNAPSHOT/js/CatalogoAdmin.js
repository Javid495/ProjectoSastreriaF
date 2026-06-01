import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { CardPrendasAdmin } from "../helpers/CardsAdmin.js"

const btnEliminar = document.querySelector("#Eliminar");
const btnAgregarC = document.querySelector("#AgregarP");
const contenedorModulo = document.getElementById('modulo-catalogo');
const contenedor = document.querySelector("#inventario-container")

function activarModoEliminar() {
    // Añade la clase; CSS se encarga de mostrar los checkboxes y los botones inferiores
    contenedorModulo.classList.add('modo-edicion');
    btnEliminar.textContent = "Cancelar";
    btnAgregarC.textContent = "Confirmar"

}

function desactivarModoEliminar() {
    // Remueve la clase; regresa el catálogo a su visualización por defecto
    contenedorModulo.classList.remove('modo-edicion');
            
    // Opcional: Desmarcar todos los checkboxes al cancelar
    const checkboxes = document.querySelectorAll('.custom-checkbox');
    checkboxes.forEach(cb => cb.checked = false);
    btnEliminar.textContent = "Eliminar Varios";
    btnAgregarC.textContent = "Agregar Prenda"
}

async function CargarCatalogoAdmin(){

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
            const nuevaCard = CardPrendasAdmin(prenda);

            contenedor.appendChild(nuevaCard);
        });
    }
    catch (error){

        console.error("Hubo algun error en la carga del catalogo actual: ", error);

        contenedor.innerHTML = "<p>Se presentar algunos errores al momento de cargar el catalog de prendas</p>"
    }
}

btnEliminar.addEventListener("click" , (e) => {

    
    if (btnEliminar.textContent == "Cancelar"){
        desactivarModoEliminar();
    }
    else{
        activarModoEliminar();
    }
})

document.addEventListener("DOMContentLoaded", (e) => {

    MostrarSide();
    CargarCatalogoAdmin();
})
