import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js"

function cargarContadoresDashboard() {
    let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    
    fetch(`${urlBase}/AdminCotizaciones?accion=contar`)
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error("Error en la respuesta del servidor");
            }
            return respuesta.json();
        })
        .then(data => {
            // 1. Métrica: Pedidos a Cotizar
            const contenedorCotizar = document.getElementById("num-pedidos-cotizar");
            if (contenedorCotizar) {
                contenedorCotizar.innerText = data.pedidosCotizar;
                if (data.pedidosCotizar > 0) {
                    contenedorCotizar.style.color = "#5d2b90"; 
                }
            }

            // 2. Métrica: Nuevos Pedidos (¡El que acabamos de agregar!)
            const contenedorNuevos = document.getElementById("num-nuevos-pedidos");
            if (contenedorNuevos) {
                contenedorNuevos.innerText = data.nuevosPedidos;
                if (data.nuevosPedidos > 0) {
                    contenedorNuevos.style.color = "#5d2b90"; 
                }
            }
        })
        .catch(error => {
            console.error("Error cargando las métricas del sastre:", error);
        });
}

function cargarPrendasBajoStock() {
    let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const contenedor = document.querySelector(".inventario-container");

    if (!contenedor) return;

    fetch(`${urlBase}/AdminCotizaciones?accion=bajoStock`)
        .then(respuesta => {
            if (!respuesta.ok) throw new Error("Error obteniendo inventario crítico");
            return respuesta.json();
        })
        .then(prendas => {
            contenedor.innerHTML = ""; // Limpiamos el placeholder estático del HTML

            if (prendas.length === 0) {
                contenedor.innerHTML = "<p class='sin-alertas'>✅ Todo el inventario se encuentra en niveles óptimos.</p>";
                return;
            }

            prendas.forEach(prenda => {
                // Validación básica de ruta de imagen por si manejas rutas relativas o absolutas
                const rutaImg = (prenda.imagen.startsWith("http") || prenda.imagen.startsWith("data:")) 
                    ? prenda.imagen : "../" + prenda.imagen;

                const articulo = document.createElement("article");
                articulo.classList.add("card-inventario");
                
                // Si el stock es 0, le podemos añadir un estilo visual extra de "Agotado"
                if (prenda.stock === 0) {
                    articulo.classList.add("card-inventario--agotado");
                }

                articulo.innerHTML = `
                    <div class="img-placeholder">
                        <img src="${rutaImg}" alt="${prenda.nombre}">
                    </div>
                    
                    <div class="info-prenda">
                        <p><strong>${prenda.nombre}</strong></p>
                        <p>Precio: $${prenda.precio.toLocaleString()}</p>
                        <p class="stock-alerta">Stock: ${prenda.stock}</p>
                    </div>

                    <div class="acciones-prenda">
                        <button class="btn-blanco btn-modificar" data-id="${prenda.id}">Modificar Detalles</button>
                        <button class="btn-blanco btn-ver-catalogo">Ver Catálogo</button>
                    </div>
                `;

                articulo.querySelector(".btn-modificar").addEventListener("click", (e) => {
                    const idPrenda = e.target.getAttribute("data-id");
    
                // Redireccionamos directo a la vista de edición usando el parámetro "id" que tu JS ya espera
                    window.location.href = `VistaModificacionProduct.html?id=${idPrenda}`; 
                });

                // 🎯 EVENTO DE REDIRECCIÓN: Redirecciona al catálogo del administrador al hacer click
                articulo.querySelector(".btn-ver-catalogo").addEventListener("click", () => {
                    window.location.href = "CatalogoAdmin.html"; // <-- Ajusta aquí el nombre exacto de tu archivo del catálogo admin
                });

                contenedor.appendChild(articulo);
            });
        })
        .catch(error => {
            console.error("Error cargando prendas bajo stock:", error);
        });
}

// Modifica tu DOMContentLoaded para incluir la nueva función
document.addEventListener("DOMContentLoaded", () => {
    MostrarSide();
    cargarContadoresDashboard();
    cargarPrendasBajoStock(); // ¡Nueva ejecución!

    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });
});


