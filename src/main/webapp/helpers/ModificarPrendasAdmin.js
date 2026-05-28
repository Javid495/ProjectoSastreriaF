
const formulario = document.querySelector("#actualizarProducto");

export function ModificarPrendas(id){

    formulario.addEventListener("submit" ,(e) => {

        //Eviitamos recargas involuntarias de parte del navegador
        e.preventDefault();

        const nombrePrenda = document.querySelector("#nombreProducto").value;
    })
}