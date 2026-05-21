import { MostrarSide } from "../helpers/RelizarPeticion.js"

MostrarSide();

const btnEliminar = document.querySelector("#Eliminar");
const btnAgregarC = document.querySelector("#AgregarP");
const contenedorModulo = document.getElementById('modulo-catalogo');

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

btnEliminar.addEventListener("click" , (e) => {

    
    if (btnEliminar.textContent == "Cancelar"){
        desactivarModoEliminar();
    }
    else{
        activarModoEliminar();
    }
})
