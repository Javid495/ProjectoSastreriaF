// TuArchivoPrincipal.js
import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { RegistrarPrendas } from "../helpers/AgregarPrendas.js";
import { cargarSelectorCategorias } from "../helpers/MostrarCategoriasAdmin.js"


document.addEventListener("DOMContentLoaded", async (e) => {
    
    // Carga de la estructura compartida de tu Admin panel
    MostrarSide();
    
    // 1. Inicializamos la escucha del evento Submit para registrar el producto
    RegistrarPrendas();

    //2. Mostramos ls categorias disponibles:
    cargarSelectorCategorias();


    // === INTERFAZ GRÁFICA: MANEJO DE IMÁGENES NUEVAS ===
    const btnAgregar = document.querySelector(".btn-subir-imagenes"); 
    const inputOculto = document.querySelector("#inputArchivoOculto");
    const contenedorImgs = document.querySelector("#contenedorImgs"); 

    if (btnAgregar && inputOculto && contenedorImgs) {
        
        // Al hacer clic en el botón "+", transferimos el evento al input oculto
        btnAgregar.addEventListener("click", (e) => {
            e.preventDefault(); 
            inputOculto.click(); 
        });

        // Captura y renderizado de las imágenes seleccionadas desde la PC
        inputOculto.addEventListener("change", (e) => {
            const archivos = e.target.files;

            if (archivos.length > 0) {
                Array.from(archivos).forEach(archivo => {
                    const lector = new FileReader();

                    lector.onload = function(eventoLector) {
                        const rutaBase64 = eventoLector.target.result;

                        // Crear el contenedor estético de la miniatura
                        const divMiniatura = document.createElement("div");
                        divMiniatura.classList.add("img-miniatura-admin");

                        // Creamos la etiqueta img para previsualizar la prenda
                        const nuevaImg = document.createElement("img");
                        nuevaImg.setAttribute("src", rutaBase64);
                
                        // AMARRE CLAVE: Guardamos el archivo binario nativo directamente en el elemento del DOM
                        nuevaImg.fileObject = archivo; 

                        // Creamos el botón de eliminar (la 'x') por si el admin se equivoca de foto
                        const botonEliminar = document.createElement("span");
                        botonEliminar.innerHTML = "×";
                        botonEliminar.classList.add("btn-eliminar-foto"); 
                
                        botonEliminar.addEventListener("click", () => {
                            divMiniatura.remove();
                        });

                        divMiniatura.appendChild(nuevaImg);
                        divMiniatura.appendChild(botonEliminar);

                        // Insertamos la miniatura antes del botón "+" para mantener el orden visual
                        contenedorImgs.insertBefore(divMiniatura, btnAgregar);
                    };

                    lector.readAsDataURL(archivo);
                });
            }
            // Limpiamos el valor para poder volver a seleccionar la misma imagen si se desea
            inputOculto.value = ""; 
        });
    }
});