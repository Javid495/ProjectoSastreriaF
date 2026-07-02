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

    // 1. Leer el carrito actual una sola vez
    const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

    lista.forEach(prendaOriginal => {
        // 2. Clonamos la prenda para no dañar los datos originales en memoria
        let prendaModificada = { ...prendaOriginal };

        // 3. Sumamos cuántas unidades de ESTA prenda en específico ya tiene el usuario en su carrito
        const unidadesEnCarrito = carrito.reduce((sum, item) => {
            // Buscamos coincidencia por idPrenda, o por nombre como plan de respaldo seguro
            const esMismaPrenda = item.idPrenda === prendaOriginal.id || item.nombre === prendaOriginal.nombre;
            return esMismaPrenda ? sum + item.cantidad : sum;
        }, 0);

        // 4. Restamos del stock original que vino de la Base de Datos
        const stockDisponibleReal = prendaOriginal.stock - unidadesEnCarrito;
        prendaModificada.stock = stockDisponibleReal < 0 ? 0 : stockDisponibleReal;

        // 5. Se lo enviamos a tu función modular original. 
        // Ella leerá 'prendaModificada.stock' y pintará el número ya restado automáticamente.
        const nuevaCard = crearCards(prendaModificada);

        // 6. Si el stock total disponible para el usuario es 0, deshabilitamos la tarjeta que nos devolvió el módulo
        if (stockDisponibleReal <= 0) {
            nuevaCard.classList.add("producto--agotado"); // Por si quieres aplicar opacidad con CSS
            
            // Buscamos el botón usando la clase exacta que le asignas en tu módulo (.card__button)
            const botonCard = nuevaCard.querySelector(".card__button");
            if (botonCard) {
                botonCard.innerText = "Sin existencias";
                botonCard.style.pointerEvents = "none"; // Bloquea el evento click de redirección
                botonCard.style.background = "#ccc";    // Estilo visual de deshabilitado
                botonCard.style.color = "#777";
            }
        }

        contenedor.appendChild(nuevaCard);
    });
}

/**
 * Aplica filtros de forma EXCLUSIVA siguiendo la arquitectura del Admin
 * (O busca por nombre, O filtra por Talla, O filtra por Tipo de prenda)
 */
function aplicarFiltros(e) {
    // [Tus limpiadores de inputs visuales se quedan exactamente igual...]
    if (e && e.target === inputBusqueda && inputBusqueda.value.trim() !== "") {
        selectTalla.value = "";
        selectTipo.value = "";
    }

    const textoBusqueda = normalizarTexto(inputBusqueda.value);
    const tallaSeleccionada = normalizarTexto(selectTalla.value);
    const tipoSeleccionado = normalizarTexto(selectTipo.value);

    // Filtrado con bifurcación lógica exclusiva
    const prendasFiltradas = todasLasPrendas.filter(prenda => {
        
        // 🛑 FILTRO FILTRADO FANTASMA: Si la prenda está inactiva en la DB, se descarta de inmediato
        if (prenda.estado === "inactiva") {
            return false; 
        }

        // PRIORIDAD 1: Búsqueda por coincidencia de texto en el nombre
        if (textoBusqueda) {
            const nombrePrenda = normalizarTexto(prenda.nombre);
            return nombrePrenda.includes(textoBusqueda);
        }

        // PRIORIDAD 2: Filtrado estricto por Talla
        if (tallaSeleccionada) {
            const tallaPrenda = normalizarTexto(prenda.talla);
            return tallaPrenda.includes(tallaSeleccionada); 
        }

        // PRIORIDAD 3: Filtrado por Tipo de Prenda (Categoría)
        if (tipoSeleccionado) {
            const categoriaPrenda = normalizarTexto(prenda.categoria || prenda.tipo);
            return categoriaPrenda === tipoSeleccionado;
        }

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