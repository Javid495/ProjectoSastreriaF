// TuArchivoPrincipal.js
import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { RegistrarPrendas } from "../helpers/AgregarPrendas.js";
import { cargarSelectorCategorias } from "../helpers/MostrarCategoriasAdmin.js"

document.addEventListener("DOMContentLoaded", async (e) => {
    
    // Carga de la estructura compartida de tu Admin panel
    MostrarSide();
    
    // 1. Inicializamos la escucha del evento Submit para registrar el producto
    // (Este helper ahora barrerá todas las filas clonadas automáticamente)
    RegistrarPrendas();

    // 2. Mostramos las categorías disponibles:
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


    // === NUEVO === INTERFAZ GRÁFICA: MANEJO DE VARIANTES DINÁMICAS (TALLAS/STOCK)
    const btnAgregarVariante = document.getElementById("btnAgregarVariante");
    const seccionVariantes = document.getElementById("seccionVariantes");

    if (btnAgregarVariante && seccionVariantes) {
        btnAgregarVariante.addEventListener("click", () => {
            // 1. Clonamos la fila modelo exacta que ya tienes maquetada en el HTML
            const nuevaFila = seccionVariantes.querySelector(".fila-variante").cloneNode(true);
            
            // 2. Limpiamos los valores clonados para que la nueva fila aparezca vacía
            nuevaFila.querySelector(".input-talla").value = "";
            nuevaFila.querySelector(".input-precio").value = "";
            nuevaFila.querySelector(".input-stock").value = "";
            
            // 3. Hacemos visible el botón de eliminar fila en este clon
            const btnEliminar = nuevaFila.querySelector(".btn-remover-variante");
            if (btnEliminar) {
                btnEliminar.classList.remove("visually-oculto");
                
                // 4. Escuchador de eventos para destruir esta fila específica si se pulsa la "×"
                btnEliminar.addEventListener("click", () => {
                    nuevaFila.remove();
                });
            }
            
            // 5. Inyectamos la nueva fila al final del contenedor de variantes
            seccionVariantes.appendChild(nuevaFila);
        });
    }

});