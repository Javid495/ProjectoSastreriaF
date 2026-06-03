// js/CatalogoAdmin.js
import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { CardPrendasAdmin } from "../helpers/CardsAdmin.js"
import { procesarEliminacionMasiva } from "../helpers/EliminarVarios.js"
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js"

const btnEliminar = document.querySelector("#Eliminar");
const btnAgregarC = document.querySelector("#AgregarP"); // Tu botón dinámico
const contenedorModulo = document.getElementById('modulo-catalogo');
const contenedor = document.querySelector("#inventario-container")

function activarModoEliminar() {
    // Añade la clase; CSS se encarga de mostrar los checkboxes y los botones inferiores
    contenedorModulo.classList.add('modo-edicion');
    btnEliminar.textContent = "Cancelar";
    btnAgregarC.textContent = "Confirmar"; // Cambia el estado del botón
}

function desactivarModoEliminar() {
    // Remueve la clase; regresa el catálogo a su visualización por defecto
    contenedorModulo.classList.remove('modo-edicion');
            
    // Desmarcar todos los checkboxes al cancelar
    const checkboxes = document.querySelectorAll('.custom-checkbox');
    checkboxes.forEach(cb => cb.checked = false);
    btnEliminar.textContent = "Eliminar Varios";
    btnAgregarC.textContent = "Agregar Prenda"; // Regresa a su estado normal
}

async function CargarCatalogoAdmin(){
    try {
        // Obtenemos la lista de productos del catalogo
        const respuesta = await fetch("../ObtenerPrendas");
        
        // Verificamos que los datos hayan llegado correctamente 
        if(!respuesta.ok) throw new Error("Error en la obtencion de datos");
    
        // Recibimos el archivo.json 
        const prendas = await respuesta.json();
    
        contenedor.innerHTML = "";
    
        // Recorremos los productos
        prendas.forEach(prenda => {
            // Llamamos la funcion crearCards y vamos creando nuestras cards
            const nuevaCard = CardPrendasAdmin(prenda);
            contenedor.appendChild(nuevaCard);
        });
    }
    catch (error){
        console.error("Hubo algun error en la carga del catalogo actual: ", error);
        contenedor.innerHTML = "<p>Se presentaron algunos errores al momento de cargar el catálogo de prendas</p>"
    }
}

// Escucha del botón Eliminar / Cancelar
btnEliminar.addEventListener("click" , (e) => {
    if (btnEliminar.textContent === "Cancelar"){
        desactivarModoEliminar();
    }
    else{
        activarModoEliminar();
    }
});


document.addEventListener("DOMContentLoaded", async (e) => {
    await MostrarSide();
    await CargarCatalogoAdmin();
    
    //Metodo para cerrar sesion desde admin
    const btnCerrar = document.querySelector("#cerrarSesion");
    
    btnCerrar.addEventListener("click", (e) =>{
        cerrarSesionServidor();
    })
});


// === NUEVA LÓGICA: Control del botón Agregar / Confirmar ===
if (btnAgregarC) {
    btnAgregarC.addEventListener("click", async(e) => {
        e.preventDefault();

        // Evaluamos el estado real basándonos en el texto que tiene puesto
        if (btnAgregarC.textContent === "Agregar Prenda") {
            // ESTADO NORMAL: Redirige al formulario de agregar que creamos hace un momento
            window.location.href = "VistaAgregarProducto.html"; 
        } 
        else if (btnAgregarC.textContent === "Confirmar") {
            // ESTADO EDICIÓN ACTIVO: El admin dio clic para eliminar los productos seleccionados
            console.log("Detectado el clic en Confirmar. Aquí irá tu función de borrado masivo.");
            await procesarEliminacionMasiva(desactivarModoEliminar);
        }
    });
}

// Buscamos el contenedor global donde se pintan todas las tarjetas de prendas
const contenedorTarjetas = document.querySelector("#inventario-container"); // Ajusta el ID de tu contenedor real

if (contenedorTarjetas) {
    contenedorTarjetas.addEventListener("click", async (e) => {
        // Detectamos si el usuario hizo clic en el botón de eliminar o en un icono dentro de él
        const botonEliminar = e.target.closest(".btnEliminarPrenda");
        const ContPreda = e.target.closest("article");
        
        
        if (botonEliminar) {
            e.preventDefault();
            
            // Extraemos el ID que dejamos guardado en la Card (ej: data-id="42")
            const idPrenda = ContPreda.getAttribute("data-id");

            console.log(idPrenda);

            // Obtenemos el nombre para personalizar la alerta
            const card = ContPreda.closest(".card-inventario"); // Ajusta a la clase de tu tarjeta
            const nombrePrenda = card ? card.querySelector("p").textContent : "esta prenda";

            // Confirmación de seguridad intuitiva
            if (confirm(`¿Estás completamente seguro de eliminar "${nombrePrenda}"? Esta acción borrará también todas sus imágenes.`)) {
                
                try {
                    // Enviamos la petición de eliminación al Servlet
                    const respuesta = await fetch(`../EliminarPrendaServlet?id=${idPrenda}`, {
                        method: "DELETE" // Usamos el método HTTP correcto para borrados
                    });

                    const resultado = await respuesta.json();

                    if (resultado.status === "Exito") {
                        alert("Prenda eliminada correctamente.");
                        // Removemos la tarjeta visualmente de inmediato sin recargar la página
                        if (card) card.remove(); 
                    } else {
                        alert("Error del servidor: " + resultado.mensaje);
                    }
                } catch (error) {
                    console.error("Error en la petición de eliminación:", error);
                    alert("No se pudo conectar con el servidor para eliminar.");
                }
            }
        }
    });
}