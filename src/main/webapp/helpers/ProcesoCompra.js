// ProcesoCompra.js
import { llamarComponente } from "../helpers/CompHtml.js"; // Importamos para pintar la ventana de éxito

export async function RealizarCompra() {
    
    // Elementos del DOM
    const txtTotal = document.querySelector("#MostrarTotal");
    const contenedorTelefono = document.querySelector("#contenedorTelefono");
    const inputTelefono = document.querySelector("#inputTelefono");
    const inputDireccion = document.querySelector("#inputDireccion");
    
    const btnNequi = document.querySelector("#btnNequi");
    const btnDaviplata = document.querySelector("#btnDaviplata");
    const btnConfirmar = document.querySelector("#ConfirmarCompra");

    let metodoPagoSeleccionado = "";

    //Mostrar el total del local storage
    const cargarTotalCompra = () => {
        //"carritoSastreria" para que coincida con tu otro archivo
        const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
        
        const granTotal = carrito.reduce((acumulado, item) => {
            return acumulado + (parseFloat(item.precio) * parseInt(item.cantidad || 1));
        }, 0);

        txtTotal.textContent = `$ ${granTotal.toLocaleString('es-CO', { minimumFractionDigits: 0 })}`;
    };

    cargarTotalCompra();

    //Habilitar el campo o input de telefono
    const activarMetodoPago = (metodo, botonActivo, botonInactivo) => {
        metodoPagoSeleccionado = metodo;
        
        botonActivo.style.border = "3px solid #5d2b90"; 
        botonInactivo.style.border = "none";

        contenedorTelefono.style.display = "flex";
        inputTelefono.focus(); 
    };

    if (btnNequi && btnDaviplata) {
        btnNequi.addEventListener("click", () => activarMetodoPago("Nequi", btnNequi, btnDaviplata));
        btnDaviplata.addEventListener("click", () => activarMetodoPago("Daviplata", btnDaviplata, btnNequi));
    }

    //Se realizar el envio de datos del formulario
    if (btnConfirmar) {
        btnConfirmar.addEventListener("click", async (e) => {
            e.preventDefault(); // Evita que la página se recargue

            const direccion = inputDireccion.value.trim();
            const telefono = inputTelefono.value.trim();
            const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];

            if (!direccion) {
                alert("Por favor, ingresa tu dirección de entrega.");
                return;
            }
            if (!metodoPagoSeleccionado) {
                alert("Debes seleccionar un método de pago antes de continuar.");
                return;
            }
            if (!telefono || telefono.length < 7) {
                alert("Por favor, ingresa un número de teléfono válido.");
                return;
            }
            if (carrito.length === 0) {
                alert("El carrito está vacío.");
                return;
            }

            //Se crea el array o elemento con los datos del formulario
            const datosCompra = {
                direccion: direccion,
                telefono: telefono,
                metodoPago: metodoPagoSeleccionado,
                tipoPedido: "Catalogo",
                productos: carrito.map(item => ({
                    idPrenda: item.id,
                    cantidad: item.cantidad || 1,
                    totalLinea: item.precio * (item.cantidad || 1)
                }))
            };

            try {
                const respuesta = await fetch("../ProcesarCompraServlet", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(datosCompra)
                });

                const resultado = await respuesta.json();

                if (resultado.status === "Exito") {
                    // Limpiamos el localStorage ya que la compra fue guardada en MySQL
                    localStorage.removeItem("carritoSastreria"); 
                    
                    // 🔄 TRANSMISIÓN LIMPIA DE VENTANAS: Ahora sí quitamos el formulario y ponemos el éxito
                    const confirmarComprarDiv = document.querySelector("#compraCarrito");
                    confirmarComprarDiv.innerHTML = ""; 
                    
                    await llamarComponente("#confirmacionPago", "../componentesWeb/VentanaComprobacion.html");
                } else {
                    alert("Error en el servidor: " + resultado.mensaje);
                }
            } catch (error) {
                console.error("Error crítico de red:", error);
                alert("No se pudo procesar la transacción.");
            }
        });
    }
}
