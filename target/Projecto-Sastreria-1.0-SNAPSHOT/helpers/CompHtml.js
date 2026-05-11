export function llamarComponente(id, contenido){
    
    fetch(contenido)
    .then(response => response.text())
    .then(contenido => {
        (id).innerHTML= contenido;        
    })

    .catch(
        error => console.error("Error al cargar el contenido")
    )
}

