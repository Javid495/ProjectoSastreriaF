export function comprobarSesion(){

    //Detectar el nombre base o funete origin del projecto
    const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/',1));

    fetch(`${urlBase}/VerificarSesion`)
    .then(response => response.json())
    .then(data => {

        if (data.logeado){

            console.log(`El usuario: ${data.nombre} con el id ${data.id}`);
            //Podemos añadir la imagen y el nombre en html a partir de aqui
        } 
        else{
            console.log("Usuario general");  
        }
    })
    .catch(error => console.error("Error al conectar con el servelt de verificacion de sesion"));


}