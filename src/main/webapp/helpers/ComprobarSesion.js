import { cerrarSesionServidor } from "./CerrarSesion.js"

export function comprobarSesion(ruta){

    //Detectar el nombre base o funete origin del projecto
    const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/',1));
    const headerPr = document.querySelector(".header__nav") 
    const ContSession = headerPr.querySelector("#SesionUser");

    fetch(`${urlBase}/VerificarSesion`)
    .then(response => response.json())
    .then(data => {

        if (data.logeado){

            console.log(`El usuario: ${data.nombre} con el id ${data.id}`);

            let imagenUser = data.imagen;

            if (ruta === "../"){
                imagenUser = `${ruta}${data.imagen}`;                
            }
            
            console.log(imagenUser);
            ContSession.innerHTML = `
            <div class="header__user-area">
                <div class="header__profile">
                    <span class="header__username">${data.nombre}</span>
                    <div class="header__avatar" id="perfilIcono">
                        <img src="${imagenUser}" alt="Perfil" onerror="this.style.display='none'">
                    </div>
                </div>

                <div id="menuDesplegable" class="header__dropdown">
                    <ul class="dropdown__list">
                        <li class="btn__cerrarsesion" id="cerrarSesion">Cerrar Sesión</li>
                    </ul>
                </div>
            </div>
            `

            const IconoPerfil = document.querySelector("#perfilIcono");
            const menuDesplegable = document.querySelector("#menuDesplegable");
            const btnCerrarSesion = document.querySelector("#cerrarSesion");

            IconoPerfil.addEventListener("click", (e) => {

                e.stopPropagation();
                if (menuDesplegable.style.display === "none") {
                    menuDesplegable.style.display = "block";
                } 
                else {
                    menuDesplegable.style.display = "none";
                }

            });

            btnCerrarSesion.addEventListener("click", (e) =>{
                cerrarSesionServidor();
            })
            
        } 

        else{
            console.log("Usuario general"); 
            console.log(window.location.href);
            
            if (window.location.href == "http://localhost:8080/Projecto-Sastreria/" || window.location.href == "http://localhost:8080/Projecto-Sastreria/index.html"){
                ContSession.innerHTML = `<a href="inicioSecion.html" class="header__item"><button class="header__login" id="BtnHeader">Inicio de sesión</button></a>`;
            }
            else{
                ContSession.innerHTML = `<a href="../inicioSecion.html" class="header__item"><button class="header__login" id="BtnHeader">Inicio de sesión</button></a>`;
            }
        }
    })
    .catch(error => console.error("hubo algun error al verificar la sesion" + error));

}


