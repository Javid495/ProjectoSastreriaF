import { llamarComponente } from "../helpers/CompHtml.js";
import { rederizarCarrito } from "../js/CarritoCompra.js";

/**
 * Procesa la confirmación de pago tanto para productos del catálogo como para cotizaciones personalizadas.
 * @param {string} tipoPedido - "Catalogo" o "A Medida"
 * @param {Object|null} datosCotizacion - { idCotizacion: number, precio: number } (Solo si es 'A Medida')
 */
export async function RealizarCompra(tipoPedido = "Catalogo", datosCotizacion = null) {
    
    // Elementos del DOM
    const txtTotal = document.querySelector("#MostrarTotal");
    const contenedorTelefono = document.querySelector("#contenedorTelefono");
    const inputTelefono = document.querySelector("#inputTelefono");
    const inputDireccion = document.querySelector("#inputDireccion");
    
    const btnNequi = document.querySelector("#btnNequi");
    const btnDaviplata = document.querySelector("#btnDaviplata");
    const btnConfirmar = document.querySelector("#ConfirmarCompra");

    const sombreado = document.querySelector(".sombreado");

    let metodoPagoSeleccionado = "";

    // 💰 Cargar el total dinámicamente dependiendo del flujo
    const cargarTotalCompra = () => {
        if (tipoPedido === "A Medida" && datosCotizacion) {
            // 🌟 Intentamos leer 'precio' o la columna de la BD 'Cotizacion_Precio' por si acaso
            let precioRaw = datosCotizacion.precio || datosCotizacion.Cotizacion_Precio;

            if (precioRaw !== undefined && precioRaw !== null) {
                // Si viene como String con "$" o puntos (ej: "$30.000"), dejamos SOLO los números
                if (typeof precioRaw === "string") {
                    precioRaw = precioRaw.replace(/[^0-9]/g, ""); 
                }
                
                const precioNumerico = parseFloat(precioRaw);

                if (!isNaN(precioNumerico)) {
                    txtTotal.textContent = `$ ${precioNumerico.toLocaleString('es-CO', { minimumFractionDigits: 0 })}`;
                    return; // Éxito, salimos de la función
                }
            }
            
            // Salvaguarda visual si algo sigue fallando con el objeto enviado
            txtTotal.textContent = "$ 0 (Error precio)";
            console.warn("datosCotizacion no contiene un precio válido:", datosCotizacion);

        } else {
            // Flujo tradicional: Leer del carrito en LocalStorage
            const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
            const granTotal = carrito.reduce((acumulado, item) => {
                return acumulado + (parseFloat(item.precio) * parseInt(item.cantidad || 1));
            }, 0);
            txtTotal.textContent = `$ ${granTotal.toLocaleString('es-CO', { minimumFractionDigits: 0 })}`;
        }
    };

    cargarTotalCompra();

    // Activar pasarelas de pago visuales
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

    // Procesar el Submit del formulario de pago
    if (btnConfirmar) {
        // Clonamos el botón o removemos listeners antiguos para evitar ejecuciones duplicadas
        const nuevoBtnConfirmar = btnConfirmar.cloneNode(true);
        btnConfirmar.parentNode.replaceChild(nuevoBtnConfirmar, btnConfirmar);

        nuevoBtnConfirmar.addEventListener("click", async (e) => {
            e.preventDefault();

            const direccion = inputDireccion.value.trim();
            const telefono = inputTelefono.value.trim();

            // Validaciones comunes de interfaz
            if (!direccion) return alert("Por favor, ingresa tu dirección de entrega.");
            if (!metodoPagoSeleccionado) return alert("Debes seleccionar un método de pago antes de continuar.");
            if (!telefono || telefono.length < 7) return alert("Por favor, ingresa un número de teléfono válido.");


            // Construcción del Payload JSON adaptable
            let datosCompra = {
                direccion: direccion,
                telefono: telefono,
                metodoPago: metodoPagoSeleccionado,
                tipoPedido: tipoPedido // "Catalogo" o "A Medida"
            };

            if (tipoPedido === "A Medida") {
                // 🎯 COLUMNA ESPEJO: Forzamos a que viaje con el nombre exacto de la base de datos
                // Usamos || por si acaso el objeto original trae el ID en minúscula o en mayúscula
                datosCompra.CotizacionPedido_Id = datosCotizacion.CotizacionPedido_Id || datosCotizacion.idCotizacion;
                datosCompra.totalLinea = datosCotizacion.precio || datosCotizacion.Cotizacion_Precio;
            } else {
                const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
                if (carrito.length === 0) return alert("El carrito está vacío.");
    
                datosCompra.productos = carrito.map(item => ({
                    idPrenda: item.id,
                    cantidad: item.cantidad || 1,
                    totalLinea: item.precio * (item.cantidad || 1)
                }));
            }

            try {
                const respuesta = await fetch("../ProcesarCompraServlet", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(datosCompra)
                });

                const resultado = await respuesta.json();

                if (resultado.status === "Exito") {
                    if (tipoPedido === "Catalogo") {
                        localStorage.removeItem("carritoSastreria"); 
                    }
                    
                    // Limpieza y renderizado de la ventana de éxito
                    const confirmarComprarDiv = document.querySelector("#compraCarrito");
                    if (confirmarComprarDiv) confirmarComprarDiv.innerHTML = ""; 
                    
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

    document.addEventListener("click", (e) => {
    if (e.target.closest("#pagoConfirmado")) {
        sombreado.classList.remove("aparecerSombreado");

        const pagoConfirm = document.querySelector(".confirmacion__pago");
        console.log(pagoConfirm);
        

        pagoConfirm.innerHTML = "";
        rederizarCarrito(); // Recargamos la vista (ahora saldrá vacía)
    }
    });
}
