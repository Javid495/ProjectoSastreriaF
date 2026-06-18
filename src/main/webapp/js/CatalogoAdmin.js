// js/CatalogoAdmin.js
import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { CardPrendasAdmin } from "../helpers/CardsAdmin.js"
import { procesarEliminacionMasiva } from "../helpers/EliminarVarios.js"
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js"

const btnEliminar = document.querySelector("#Eliminar");
const btnAgregarC = document.querySelector("#AgregarP");
const contenedorModulo = document.getElementById('modulo-catalogo');
const contenedor = document.querySelector("#inventario-container");

// Selectores de búsqueda y filtros (Talla eliminada)
const inputBusqueda = document.querySelector(".search__input");
const selectCategoria = document.querySelector("#category"); 

let todasLasPrendas = []; // Respaldo original de prendas provenientes de la BD

function activarModoEliminar() {
    contenedorModulo.classList.add('modo-edicion');
    btnEliminar.textContent = "Cancelar";
    btnAgregarC.textContent = "Confirmar";
}

function desactivarModoEliminar() {
    contenedorModulo.classList.remove('modo-edicion');
    const checkboxes = document.querySelectorAll('.custom-checkbox');
    checkboxes.forEach(cb => cb.checked = false);
    btnEliminar.textContent = "Eliminar Varios";
    btnAgregarC.textContent = "Agregar Prenda";
}

/**
 * Función Helper: Convierte a minúsculas, remueve espacios extras y elimina tildes/acentos.
 */
const normalizarTexto = (texto) => {
    if (!texto) return "";
    return texto
        .toLowerCase()
        .trim()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, ""); 
};

// Carga las categorías desde la base de datos y llena el <select>
async function CargarCategoriasAdmin() {
    try {
        const respuesta = await fetch("../ObtenerCategorias"); 
        if (!respuesta.ok) throw new Error("Error al obtener las categorías");

        const categorias = await respuesta.json(); 

        selectCategoria.innerHTML = '<option value="">Seleccionar</option>';

        categorias.forEach(cat => {
            const option = document.createElement("option");
            option.value = cat.nombre; 
            option.textContent = cat.nombre;
            selectCategoria.appendChild(option);
        });
    } catch (error) {
        console.error("Error al renderizar el select de categorías:", error);
    }
}

// Pinta las tarjetas en el contenedor
function renderizarPrendas(lista) {
    contenedor.innerHTML = "";

    if (lista.length === 0) {
        contenedor.innerHTML = "<p class='sin-resultados'>🔍 No se encontraron prendas con los filtros seleccionados.</p>";
        return;
    }

    lista.forEach(prenda => {
        const nuevaCard = CardPrendasAdmin(prenda);
        contenedor.appendChild(nuevaCard);
    });
}

/**
 * Aplica filtros de forma EXCLUSIVA (O busca por Nombre O filtra por Categoría)
 */
function aplicarFiltros(e) {
    // Interceptamos cuál control disparó el evento para limpiar el otro visualmente
    if (e && e.target === inputBusqueda && inputBusqueda.value.trim() !== "") {
        selectCategoria.value = ""; // Si escribe, resetea la categoría
    }
    if (e && e.target === selectCategoria && selectCategoria.value !== "") {
        inputBusqueda.value = ""; // Si selecciona categoría, limpia el buscador
    }

    const textoBusqueda = normalizarTexto(inputBusqueda.value);
    const categoriaSeleccionada = normalizarTexto(selectCategoria.value);

    // Filtrado con bifurcación lógica exclusiva
    const prendasFiltradas = todasLasPrendas.filter(prenda => {
        
        // PRIORIDAD 1: Si hay texto en el buscador, filtra estrictamente por nombre
        if (textoBusqueda) {
            const nombrePrenda = normalizarTexto(prenda.nombre);
            return nombrePrenda.includes(textoBusqueda);
        }

        // PRIORIDAD 2: Si se seleccionó una categoría, filtra por coincidencia exacta
        if (categoriaSeleccionada) {
            const categoriaPrenda = normalizarTexto(prenda.categoria);
            return categoriaPrenda === categoriaSeleccionada;
        }

        // Si ambos campos quedan vacíos, se muestran todos los productos
        return true;
    });

    renderizarPrendas(prendasFiltradas);
}

async function CargarCatalogoAdmin(){
    try {
        const respuesta = await fetch("../ObtenerPrendas?rol=admin");
        if(!respuesta.ok) throw new Error("Error en la obtencion de datos");
    
        todasLasPrendas = await respuesta.json();
        renderizarPrendas(todasLasPrendas);
    }
    catch (error){
        console.error("Hubo un error en la carga del catálogo actual: ", error);
        contenedor.innerHTML = "<p>Se presentaron algunos errores al cargar el catálogo.</p>"
    }
}

// Botón Cancelar / Eliminar Varios
btnEliminar.addEventListener("click" , (e) => {
    if (btnEliminar.textContent === "Cancelar") desactivarModoEliminar();
    else activarModoEliminar();
});

// Inicialización del DOM
document.addEventListener("DOMContentLoaded", async (e) => {
    await MostrarSide();
    
    await Promise.all([
        CargarCategoriasAdmin(),
        CargarCatalogoAdmin()
    ]);
    
    // Escuchadores asignados pasándoles el evento 'e' implícitamente
    if (inputBusqueda) inputBusqueda.addEventListener("input", aplicarFiltros);
    if (selectCategoria) selectCategoria.addEventListener("change", aplicarFiltros);

    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });
});

// ============================================================================
// Control del botón Agregar / Confirmar masivo (CORREGIDO)
// ============================================================================
if (btnAgregarC) {
    btnAgregarC.addEventListener("click", async (e) => {
        e.preventDefault();
        if (btnAgregarC.textContent === "Agregar Prenda") {
            window.location.href = "VistaAgregarProducto.html"; 
        } 
        else if (btnAgregarC.textContent === "Confirmar") {
            // Capturamos el booleano devuelto por el helper
            const seEliminoElLote = await procesarEliminacionMasiva(desactivarModoEliminar);
    
            // 🌟 Solo si la operación fue exitosa, limpiamos la memoria local sincronizando con la BD
            if (seEliminoElLote) {
                await CargarCatalogoAdmin(); 
            }
        }
    });
}

// ============================================================================
// Delegación de eventos para eliminación individual (REVISADO)
// ============================================================================
if (contenedor) {
    contenedor.addEventListener("click", async (e) => {
        const botonEliminar = e.target.closest(".btnEliminarPrenda");
        const ContPreda = e.target.closest("article");
        
        if (botonEliminar) {
            e.preventDefault();
            const idPrenda = ContPreda.getAttribute("data-id");
            const card = ContPreda.closest(".card-inventario"); 
            
            // Un pequeño seguro por si el <p> no existe o cambia en el HTML helper
            const pNombre = card ? card.querySelector("p") : null;
            const nombrePrenda = pNombre ? pNombre.textContent : "esta variante";

            if (confirm(`¿Seguro que deseas eliminar "${nombrePrenda}"?`)) {
                try {
                    const respuesta = await fetch(`../EliminarPrendaServlet?id=${idPrenda}`, { method: "DELETE" });
                    const resultado = await respuesta.json();

                    if (resultado.status === "Exito") {
                        alert("Prenda deshabilitada con éxito.");
                        
                        // Aquí lo manejas excelente en memoria local sin re-fetch:
                        todasLasPrendas = todasLasPrendas.filter(p => p.id !== parseInt(idPrenda));
                        aplicarFiltros(); 
                    } else {
                        alert("Error: " + resultado.mensaje);
                    }
                } catch (error) {
                    console.error("Error al eliminar prenda individual en frontend:", error);
                    alert("No se pudo conectar con el servidor de eliminación.");
                }
            }
        }
    });
}