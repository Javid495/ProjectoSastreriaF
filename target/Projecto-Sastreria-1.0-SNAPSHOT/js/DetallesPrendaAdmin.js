import { MostrarSide } from "../helpers/RelizarPeticion.js"
import { CargarDetallesAdmin } from "../helpers/CargarDetallesAdmin.js";
import { ModificarPrendas } from "../helpers/ModificarPrendasAdmin.js";


const ContAside = document.querySelector(".sidebar");

document.addEventListener("DOMContentLoaded", (e) => {

    MostrarSide();
    
    const url = new URLSearchParams(window.location.search);

    const PrendaId = url.get("id");

    if (PrendaId){
        CargarDetallesAdmin(PrendaId);

        ModificarPrendas(PrendaId);
    }

    const btnAgregar = document.querySelector(".btn-subir-imagenes"); // Ajusta al selector de tu botón blanco
    const inputOculto = document.querySelector("#inputArchivoOculto");
    const contenedorImgs = document.querySelector("#contenedorImgs"); // El contenedor gris de tu imagen 3

    if (btnAgregar && inputOculto) {
    // Al hacer clic en el botón "Agregar imágenes", transferimos el clic al input oculto
        btnAgregar.addEventListener("click", (e) => {
            e.preventDefault(); // Evita cualquier comportamiento extraño o submit
            inputOculto.click(); 
        });

        // Cuando el administrador selecciona las fotos en su ordenador...
        inputOculto.addEventListener("change", (e) => {
            const archivos = e.target.files;

            if (archivos.length > 0) {
                Array.from(archivos).forEach(archivo => {
                    const lector = new FileReader();

                    lector.onload = function(eventoLector) {
                        const rutaBase64 = eventoLector.target.result;

                        // Crear el contenedor de la miniatura
                        const divMiniatura = document.createElement("div");
                        divMiniatura.classList.add("img-miniatura-admin");

                        // Creamos la etiqueta img
                        const nuevaImg = document.createElement("img");
                        nuevaImg.setAttribute("src", rutaBase64);
                
                        //Aquí es donde se amarra el archivo físico
                        nuevaImg.fileObject = archivo; 
                        nuevaImg.setAttribute("data-nuevo", "true"); 

                        // Creamos el botón de eliminar (la 'x')
                        const botonEliminar = document.createElement("span");
                        botonEliminar.innerHTML = "×";
                        botonEliminar.classList.add("btn-eliminar-foto"); 
                
                        botonEliminar.addEventListener("click", () => {
                            divMiniatura.remove();
                        });

                        divMiniatura.appendChild(nuevaImg);
                        divMiniatura.appendChild(botonEliminar);

                        // Lo acoplamos al contenedor gris
                        contenedorImgs.appendChild(divMiniatura);
                    };

                    lector.readAsDataURL(archivo);
        });
    }
    // Limpiar el input para permitir subir el mismo archivo consecutivamente si se desea
    inputOculto.value = ""; 
        });
    }
})

