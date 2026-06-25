import { llamarComponente } from "../helpers/CompHtml.js";
import { rederizarCarrito } from "../js/CarritoCompra.js";

export async function RealizarCompra(tipoPedido = "Catalogo", datosCotizacion = null) {
    
    // 🌟 SEPARACIÓN DE SELECTORES: Ahora controlamos con precisión quirúrgica cada contenedor
    const panelProductos = document.querySelector("#mostrarCompra"); // El fondo con las cards
    const panelFormularioCompra = document.querySelector("#compraCarrito"); // El formulario flotante de pago
    const contenedorConfirmacion = document.querySelector("#confirmacionPago"); // El modal de éxito

    if (!panelFormularioCompra) {
        console.warn("No se encontró el contenedor del formulario (#compraCarrito)");
        return; 
    }

    // Elementos del DOM internos del formulario
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

            const direccion = inputDireccion ? inputDireccion.value.trim() : "";
            const telefono = inputTelefono ? inputTelefono.value.trim() : "";

            if (!direccion) return alert("Por favor, ingresa tu dirección de entrega.");
            if (!metodoPagoSeleccionado) return alert("Debes seleccionar un método de pago antes de continuar.");
            if (!telefono || telefono.length < 7 || isNaN(telefono)) {
                return alert("Por favor, ingresa un número de teléfono válido.");
            }

            let datosCompra = {
                accion: "confirmar", 
                direccion: direccion,
                telefono: telefono,
                metodoPago: metodoPagoSeleccionado,
                tipoPedido: tipoPedido 
            };

            if (tipoPedido === "A Medida") {
                const idCotizacionReal = parseInt(datosCotizacion.idCotizacion || datosCotizacion.CotizacionPedido_Id || datosCotizacion.CotizacionPedido_id || 0);
                if (idCotizacionReal === 0 || isNaN(idCotizacionReal)) {
                    return alert("Error crítico: No se detectó el ID real de la cotización.");
                }
                datosCompra.CotizacionPedido_Id = idCotizacionReal;
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
                    
                    // 🌟 AQUÍ OCULTAMOS EL FORMULARIO DE PAGO PARA QUE NO QUEDE POR DETRÁS
                    panelFormularioCompra.innerHTML = ""; 
                    panelFormularioCompra.style.display = "none";
                    
                    const layoutCompraCompleto = document.querySelector(".seccion-compra") || document.querySelector(".checkout-container");
                    if (layoutCompraCompleto) layoutCompraCompleto.style.display = "none";
                    
                    // Mostrar modal de éxito
                    await llamarComponente("#confirmacionPago", "../componentesWeb/VentanaComprobacion.html");
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
            
            // Vaciamos el contenedor del modal de éxito
            if (contenedorConfirmacion) contenedorConfirmacion.innerHTML = ""; 
            
            // 🌟 AQUÍ NOS ASEGURAMOS DE LIMPIAR POR COMPLETO EL FORMULARIO DE PAGO SIEMPRE
            if (panelFormularioCompra) {
                panelFormularioCompra.innerHTML = "";
                panelFormularioCompra.style.display = "none";
            }
            
            // Si es flujo de catálogo, refrescamos la vista principal
            if (tipoPedido === "Catalogo" && panelProductos) {
                panelProductos.style.display = "block";
                panelProductos.innerHTML = "<div style='text-align:center; padding: 40px;'><h2>¡Gracias por tu compra!</h2><p>Tu pedido ha sido registrado con éxito en ModaS.</p></div>";
                if (typeof rederizarCarrito === "function") rederizarCarrito(); 
            } else {
                // Para pedidos personalizados, recargar limpia el DOM de forma impecable
                window.location.reload();
            }
        }
    });
}

