import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js"

function cargarContadoresDashboard() {
    // Calculamos la ruta base de manera dinámica como lo tienes en las otras vistas
    let urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    
    // Llamamos al mismo servlet de cotizaciones con la acción 'contar'
    fetch(`${urlBase}/AdminCotizaciones?accion=contar`)
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error("Error en la respuesta del servidor");
            }
            return respuesta.json();
        })
        .then(data => {
            const contenedorNumero = document.getElementById("num-pedidos-cotizar");
            if (contenedorNumero) {
                // Inyectamos el valor real de la base de datos
                contenedorNumero.innerText = data.cantidad;
                
                // Opcional: Si hay más de 0 pedidos, podemos cambiar el color del número para alertar
                if (data.cantidad > 0) {
                    contenedorNumero.style.color = "#5d2b90"; 
                }
            }
        })
        .catch(error => {
            console.error("Error cargando las métricas del sastre:", error);
        });
}

document.addEventListener("DOMContentLoaded", () => {
    MostrarSide();
    cargarContadoresDashboard();

    //Metodo para cerrar sesion desde admin
    const btnCerrar = document.querySelector("#cerrarSesion");
    
    btnCerrar.addEventListener("click", (e) =>{
        cerrarSesionServidor();
    })

});

