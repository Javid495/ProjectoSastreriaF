import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { CargarDetallesAdmin } from "../helpers/CargarDetallesAdmin.js";
import { ModificarPrendas } from "../helpers/ModificarPrendasAdmin.js";
import { PintarFilaVariante } from "../helpers/AñadirModiPrendas.js"; // <-- NUEVO: Importamos el helper

const ContAside = document.querySelector(".sidebar");

document.addEventListener("DOMContentLoaded", (e) => {

    MostrarSide();
    
    const url = new URLSearchParams(window.location.search);
    const PrendaId = url.get("id");

    if (PrendaId){
        CargarDetallesAdmin(PrendaId);
        ModificarPrendas(PrendaId);
    }

    // --- NUEVO: Escuchador para Añadir Nuevas Filas de Tallaje ---
    const btnAgregarVariante = document.querySelector("#btnAgregarVariante");
    const contenedorVariantes = document.querySelector("#contenedorVariantes");

    if (btnAgregarVariante && contenedorVariantes) {
        btnAgregarVariante.addEventListener("click", (e) => {
            e.preventDefault();
            // Creamos una fila vacía para que el usuario digite una nueva combinación
            PintarFilaVariante(contenedorVariantes, null, "", 0, 0);
        });
    }

    // --- LÓGICA DE IMÁGENES (Tu código original intacto con selectores corregidos) ---
    // Agregamos el id '#btnSubirFotos' en el HTML al de las imágenes para que no choque con el de tallas
    const btnAgregarImg = document.querySelector("#btnSubirFotos") || document.querySelector(".imagenes-galeria-container .btn-subir-imagenes"); 
    const inputOculto = document.querySelector("#inputArchivoOculto");
    const contenedorImgs = document.querySelector("#contenedorImgs"); 

    if (btnAgregarImg && inputOculto) {
        // Al hacer clic en el botón "Agregar imágenes", transferimos el clic al input oculto
        btnAgregarImg.addEventListener("click", (e) => {
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
                
                        // Aquí es donde se amarra el archivo físico
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
});

