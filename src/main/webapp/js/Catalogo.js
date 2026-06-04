import { crearCards } from "../helpers/Cards.js";
import { aparecerCont } from "../helpers/RelizarPeticion.js";
import { comprobarSesion } from "../helpers/ComprobarSesion.js"; 

const contenedor = document.querySelector(".popular__cards");

// Selectores seguros para evitar el conflicto del ID duplicado en el HTML
const inputBusqueda = document.querySelector(".search__input");
const selectTalla = document.querySelector(".filter__select:not(.filter--ti)");
const selectTipo = document.querySelector(".filter--ti");

let todasLasPrendas = []; // Respaldo original de las prendas que vienen de la Base de Datos

/**
 * Función Helper: Convierte a minúsculas, remueve espacios y elimina tildes.
 */
const normalizarTexto = (texto) => {
    if (!texto) return "";
    return texto
        .toLowerCase()
        .trim()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, ""); 
};

// Se encarga exclusivamente de pintar las tarjetas en la interfaz web
function renderizarPrendas(lista) {
    contenedor.innerHTML = "";

    if (lista.length === 0) {
        contenedor.innerHTML = "<p class='sin-resultados'>🔍 No se encontraron prendas que coincidan con tu búsqueda.</p>";
        return;
    }

    lista.forEach(prenda => {
        const nuevaCard = crearCards(prenda);
        contenedor.appendChild(nuevaCard);
    });
}

/**
 * Aplica filtros de forma EXCLUSIVA siguiendo la arquitectura del Admin
 * (O busca por nombre, O filtra por Talla, O filtra por Tipo de prenda)
 */
function aplicarFiltros(e) {
    // Si el usuario escribe en la barra de búsqueda, limpiamos los selects visualmente
    if (e && e.target === inputBusqueda && inputBusqueda.value.trim() !== "") {
        selectTalla.value = "";
        selectTipo.value = "";
    }
    // Si selecciona una talla, limpiamos la barra de búsqueda y el tipo de prenda
    if (e && e.target === selectTalla && selectTalla.value !== "") {
        inputBusqueda.value = "";
        selectTipo.value = "";
    }
    // Si selecciona un tipo de prenda, limpiamos la barra de búsqueda y la talla
    if (e && e.target === selectTipo && selectTipo.value !== "") {
        inputBusqueda.value = "";
        selectTalla.value = "";
    }

    const textoBusqueda = normalizarTexto(inputBusqueda.value);
    const tallaSeleccionada = normalizarTexto(selectTalla.value);
    const tipoSeleccionado = normalizarTexto(selectTipo.value);

    // Filtrado con bifurcación lógica exclusiva
    const prendasFiltradas = todasLasPrendas.filter(prenda => {
        
        // PRIORIDAD 1: Búsqueda por coincidencia de texto en el nombre
        if (textoBusqueda) {
            const nombrePrenda = normalizarTexto(prenda.nombre);
            return nombrePrenda.includes(textoBusqueda);
        }

        // PRIORIDAD 2: Filtrado estricto por Talla
        if (tallaSeleccionada) {
            const tallaPrenda = normalizarTexto(prenda.talla);
            return tallaPrenda === tallaSeleccionada;
        }

        // PRIORIDAD 3: Filtrado por Tipo de Prenda (Categoría)
        if (tipoSeleccionado) {
            // Evaluamos tanto prenda.categoria como prenda.tipo por seguridad en tu mapeo de objetos
            const categoriaPrenda = normalizarTexto(prenda.categoria || prenda.tipo);
            return categoriaPrenda === tipoSeleccionado;
        }

        // Si todos los filtros están vacíos, retorna la lista completa
        return true;
    });

    renderizarPrendas(prendasFiltradas);
}

async function cargarCatalogo(){
    try {
        const respuesta = await fetch("../ObtenerPrendas");
        if(!respuesta.ok) throw new Error("Error en la obtención de datos");

        todasLasPrendas = await respuesta.json();
        
        // Primera renderización con todo el catálogo disponible
        renderizarPrendas(todasLasPrendas);
    }
    catch (error){
        console.error("Hubo algún error en la carga del catálogo: ", error);
        contenedor.innerHTML = "<p>No se cargaron las prendas correctamente. Por favor, vuelva a intentarlo más tarde.</p>";
    }
}

// Inicialización del DOM y asignación de escuchadores de eventos
document.addEventListener("DOMContentLoaded", async (e) => {
    e.preventDefault();
    
    await aparecerCont("../");
    comprobarSesion("../");
    await cargarCatalogo();

    // Vinculamos los eventos para que llamen a aplicarFiltros dinámicamente
    if (inputBusqueda) inputBusqueda.addEventListener("input", aplicarFiltros);
    if (selectTalla) selectTalla.addEventListener("change", aplicarFiltros);
    if (selectTipo) selectTipo.addEventListener("change", aplicarFiltros);
});