import { aparecerCont } from "../helpers/RelizarPeticion.js"; 
import { comprobarSesion } from "../helpers/ComprobarSesion.js";
import { crearCards } from "../helpers/Cards.js"; 

// Agregamos 'async' a la función flecha para poder usar 'await' dentro
document.addEventListener("DOMContentLoaded", async () => {
    
    // Corregido: Se eliminó e.preventDefault() ya que DOMContentLoaded no maneja eventos de envío o recarga.
    await aparecerCont("./");
    
    comprobarSesion("./");
    
    cargarPrendasPopulares();
});

async function cargarPrendasPopulares() {
    try {
        // 1. Petición al Servlet
        const response = await fetch("ObtenerPopularesInicio");
        
        if (!response.ok) {
            throw new Error("Error al obtener los productos populares");
        }
        

        const prendas = await response.json();
        
        const contenedorCards = document.querySelector(".popular__cards");
        if (!contenedorCards) return;

        // 🔥 ESTRATEGIA SEGURA: Eliminamos SOLO las tarjetas estáticas antiguas 
        // para NO borrar por accidente el botón izquierdo (.button-left)
        const tarjetasAntiguas = contenedorCards.querySelectorAll(".card");
        tarjetasAntiguas.forEach(tarjeta => tarjeta.remove());

        if (prendas.length === 0) {
            const mensaje = document.createElement("p");
            mensaje.textContent = "No hay prendas populares disponibles en este momento.";
            mensaje.style.padding = "20px";
            contenedorCards.appendChild(mensaje);
            return;
        }

        // 1. Traemos el carrito actual para calcular existencias reales
        const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

        prendas.forEach(prendaOriginal => {
            // 2. Clonamos y calculamos el stock disponible en base al carrito
            let prendaModificada = { ...prendaOriginal };
            const unidadesEnCarrito = carrito.reduce((sum, item) => {
                return (item.idPrenda === prendaOriginal.id || item.nombre === prendaOriginal.nombre) ? sum + item.cantidad : sum;
            }, 0);
    
            const stockDisponibleReal = prendaOriginal.stock - unidadesEnCarrito;
            prendaModificada.stock = stockDisponibleReal < 0 ? 0 : stockDisponibleReal;

            // 3. Pasamos la prenda con el stock corregido a tu módulo
            const tarjetaElemento = crearCards(prendaModificada, true); 
            contenedorCards.appendChild(tarjetaElemento);
        });

        // 2. Renderizado dinámico utilizando tu función helper adaptada
        prendas.forEach(prenda => {
            // Pasamos 'true' como segundo parámetro indicando que estamos en la RAÍZ del proyecto
            const tarjetaElemento = crearCards(prenda, true);
            
            // Insertamos la tarjeta limpia dentro del riel
            contenedorCards.appendChild(tarjetaElemento);
        });

        // 3. Activamos el movimiento físico del carrusel una vez los elementos ya existen
        inicializarMovimientoCarrusel();

    } catch (error) {
        console.error("Hubo un fallo en la carga del carrusel dinámico:", error);
    }
}

/**
 * Maneja el desplazamiento físico hacia la izquierda y derecha del carrusel de manera fluida
 */
function inicializarMovimientoCarrusel() {
    const btnIzquierdo = document.querySelector(".button-left");
    const btnDerecho = document.querySelector(".button-right"); // 👈 Asegúrate de que tenga la 'd' final
    const contenedorCards = document.querySelector(".popular__cards");

    if (!btnIzquierdo || !btnDerecho || !contenedorCards) retur

    // Calculamos dinámicamente cuánto scroll mover basándonos en el ancho de una tarjeta física
    const calcularDesplazamiento = () => {
        const tarjeta = contenedorCards.querySelector(".card");
        return tarjeta ? tarjeta.clientWidth + 20 : 300; // 20px representa el gap aproximado de separación
    };

    // Evento botón derecho (Desplazar hacia adelante)
    btnDerecho.addEventListener("click", () => {
        contenedorCards.scrollBy({
            left: calcularDesplazamiento(),
            behavior: "smooth"
        });
    });

    // Evento botón izquierdo (Desplazar hacia atrás)
    btnIzquierdo.addEventListener("click", () => {
        contenedorCards.scrollBy({
            left: -calcularDesplazamiento(),
            behavior: "smooth"
        });
    });
}



