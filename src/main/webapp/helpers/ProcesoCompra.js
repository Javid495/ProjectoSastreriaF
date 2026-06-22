import { llamarComponente } from "../helpers/CompHtml.js";
import { rederizarCarrito } from "../js/CarritoCompra.js";

/**
 * Procesa la confirmación de pago tanto para productos del catálogo como para cotizaciones personalizadas.
 * @param {string} tipoPedido - "Catalogo" o "A Medida"
 * @param {Object|null} datosCotizacion - { idCotizacion: number, precio: number } (Solo si es 'A Medida')
 */
export async function RealizarCompra(tipoPedido = "Catalogo", datosCotizacion = null) {
    
    // Detectar dinámicamente cuál contenedor está activo en el DOM actual
    let contenedorCarrito = document.querySelector("#mostrarCompra");
    if (!contenedorCarrito) {
        contenedorCarrito = document.querySelector("#compraCarrito");
    }

    if (!contenedorCarrito) {
        console.warn("No se encontró ningún contenedor de compra válido (#mostrarCompra o #compraCarrito)");
        return; 
    }

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

    // Cargar el total dinámicamente dependiendo del flujo
    const cargarTotalCompra = () => {
        if (tipoPedido === "A Medida" && datosCotizacion) {
            let precioRaw = datosCotizacion.precio || datosCotizacion.Cotizacion_Precio;

            if (precioRaw !== undefined && precioRaw !== null) {
                if (typeof precioRaw === "string") {
                    precioRaw = precioRaw.replace(/[^0-9]/g, ""); 
                }
                const precioNumerico = parseFloat(precioRaw);

                if (!isNaN(precioNumerico)) {
                    txtTotal.textContent = `$ ${precioNumerico.toLocaleString('es-CO', { minimumFractionDigits: 0 })}`;
                    return;
                }
            }
            txtTotal.textContent = "$ 0 (Error precio)";
            console.warn("datosCotizacion no contiene un precio válido:", datosCotizacion);

        } else {
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
        if (contenedorTelefono) contenedorTelefono.style.display = "flex";
        if (inputTelefono) inputTelefono.focus(); 
    };

    if (btnNequi && btnDaviplata) {
        btnNequi.addEventListener("click", () => activarMetodoPago("Nequi", btnNequi, btnDaviplata));
        btnDaviplata.addEventListener("click", () => activarMetodoPago("Daviplata", btnDaviplata, btnNequi));
    }

    // Procesar el Submit del formulario de pago
    if (btnConfirmar) {
        const nuevoBtnConfirmar = btnConfirmar.cloneNode(true);
        btnConfirmar.parentNode.replaceChild(nuevoBtnConfirmar, btnConfirmar);

        nuevoBtnConfirmar.addEventListener("click", async (e) => {
            e.preventDefault();

            // 🌟 VALIDACIÓN BLINDADA: Extraemos y limpiamos espacios vacíos inmediatamente
            const direccion = inputDireccion ? inputDireccion.value.trim() : "";
            const telefono = inputTelefono ? inputTelefono.value.trim() : "";

            if (!direccion || direccion === "") {
                alert("Por favor, ingresa tu dirección de entrega.");
                return; // Detiene la ejecución
            }
            if (!metodoPagoSeleccionado || metodoPagoSeleccionado === "") {
                alert("Debes seleccionar un método de pago antes de continuar.");
                return; // Detiene la ejecución
            }
            if (!telefono || telefono.length < 7 || isNaN(telefono)) {
                alert("Por favor, ingresa un número de teléfono válido (mínimo 7 dígitos numéricos).");
                return; // Detiene la ejecución
            }

            // CONSTRUCCIÓN DEL PAYLOAD ADAPTABLE
            let datosCompra = {
                accion: "confirmar", 
                direccion: direccion,
                telefono: telefono,
                metodoPago: metodoPagoSeleccionado,
                tipoPedido: tipoPedido 
            };

            if (tipoPedido === "A Medida") {
                const idCotizacionReal = parseInt(
                    datosCotizacion.idCotizacion || 
                    datosCotizacion.CotizacionPedido_Id || 
                    datosCotizacion.CotizacionPedido_id || 
                    0
                );

                if (idCotizacionReal === 0 || isNaN(idCotizacionReal)) {
                    console.error("❌ Error: Se intentó procesar una cotización sin un ID válido.", datosCotizacion);
                    return alert("Error crítico: No se detectó el ID real de la cotización. Revisa el botón de pago.");
                }

                datosCompra.CotizacionPedido_Id = idCotizacionReal;
                datosCompra.totalLinea = datosCotizacion.precio || datosCotizacion.Cotizacion_Precio;
            } 
            else {
                const carrito = JSON.parse(localStorage.getItem("carritoSastreria")) || [];
                if (carrito.length === 0) return alert("El carrito está vacío.");
    
                datosCompra.productos = carrito.map(item => ({
                    idPrenda: item.id,
                    id: item.id, 
                    cantidad: item.cantidad || 1,
                    totalLinea: item.precio * (item.cantidad || 1)
                }));
            }

            try {
                nuevoBtnConfirmar.disabled = true;
                nuevoBtnConfirmar.textContent = "Procesando...";

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
                    
                    // 🌟 SOLUCIÓN VISUAL: Ocultamos el contenedor principal de la compra por completo 
                    // para que los paneles laterales del total no queden flotando detrás del modal.
                    if (contenedorCarrito) {
                        contenedorCarrito.style.display = "none";
                    }
                    
                    // Si tienes un contenedor padre o una sección envolvente para toda la vista de checkout, 
                    // la ocultamos para asegurar limpieza total en pantalla:
                    const layoutCompraCompleto = document.querySelector(".seccion-compra") || document.querySelector(".checkout-container");
                    if (layoutCompraCompleto) {
                        layoutCompraCompleto.style.display = "none";
                    }
                    
                    // Mostrar modal de éxito
                    await llamarComponente("#confirmacionPago", "../componentesWeb/VentanaComprobacion.html");
                    
                    // Forzar que aparezca el sombreado oscuro del modal si aplica
                    if (sombreado) sombreado.classList.add("aparecerSombreado");

                } else {
                    alert("Error en el servidor: " + resultado.mensaje);
                    nuevoBtnConfirmar.disabled = false;
                    nuevoBtnConfirmar.textContent = "Confirmar Compra";
                }
            } catch (error) {
                console.error("Error crítico de red:", error);
                alert("No se pudo procesar la transacción.");
                nuevoBtnConfirmar.disabled = false;
                nuevoBtnConfirmar.textContent = "Confirmar Compra";
            }
        });
    }

    // Escuchador global para cerrar el modal de éxito de pago
    document.addEventListener("click", (e) => {
        if (e.target.closest("#pagoConfirmado")) {
            if (sombreado) sombreado.classList.remove("aparecerSombreado");
            
            // 🌟 SOLUCIÓN DE CIERRE: Vaciamos explícitamente el contenedor donde inyectaste el componente
            const contenedorModal = document.querySelector("#confirmacionPago");
            if (contenedorModal) {
                contenedorModal.innerHTML = ""; 
            }
            
            // También vaciamos la clase interna por si acaso estructural
            const pagoConfirmClass = document.querySelector(".confirmacion__pago");
            if (pagoConfirmClass) {
                pagoConfirmClass.innerHTML = ""; 
            }
            
            // Si es flujo de carrito, refrescamos el estado dinámico
            if (typeof rederizarCarrito === "function" && document.querySelector("#mostrarCompra")) {
                // Volvemos a hacer visible el contenedor principal para que muestre el mensaje de "Carrito Vacío"
                if (contenedorCarrito) {
                    contenedorCarrito.style.display = "block";
                    contenedorCarrito.innerHTML = "<div style='text-align:center; padding: 40px;'><h2>¡Gracias por tu compra!</h2><p>Tu pedido ha sido registrado con éxito en ModaS.</p></div>";
                }
                rederizarCarrito(); 
            } else {
                // En pedidos personalizados o fallas de contexto, recargar limpia todo perfectamente
                window.location.reload();
            }
        }
    });
}

