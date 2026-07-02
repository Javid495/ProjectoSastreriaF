import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { CardPagosAdmin } from "../helpers/CardsPagosAdmin.js";
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";

// Selectores de UI
const tabUltimosPagos = document.getElementById('tab-ultimos-pagos');
const tabMetricas = document.getElementById('tab-metricas'); // Sincronizado con tu HTML
const panelResumenGanancias = document.getElementById('resumenGanancias');
const selectPeriodo = document.getElementById('periodo-caja');
const contenedorPagos = document.getElementById('contenedorPagos');

// Elementos de los KPIs
const montoDiario = document.getElementById('monto-diario');
const montoSemanal = document.getElementById('monto-semanal');
const montoMensual = document.getElementById('monto-mensual');

// Enrutamiento dinámico inteligente según el contexto del servidor
const urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
let todosLosPagos = []; 

const formatearDinero = (valor) => {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
};

// Carga unificada de datos desde el Servlet
async function CargarDatosCaja() {
    try {
        // Usamos urlBase para asegurar que apunte a /ModaS/ObtenerHistorialPagos
        const respuesta = await fetch(`${urlBase}/ObtenerHistorialPagos`);
        if (!respuesta.ok) throw new Error("Error obteniendo el historial de transacciones");

        const datos = await respuesta.json();
        console.log("📊 Datos validados del servidor:", datos);

        // Inyectar valores en los cuadros KPI de ganancias
        if (montoDiario) montoDiario.textContent = formatearDinero(datos.metricas.diario);
        if (montoSemanal) montoSemanal.textContent = formatearDinero(datos.metricas.semanal);
        if (montoMensual) montoMensual.textContent = formatearDinero(datos.metricas.mensual);

        todosLosPagos = datos.listaPagos;
        renderizarPagos(todosLosPagos);

    } catch (error) {
        console.error("Error crítico en CargarDatosCaja:", error);
        if (contenedorPagos) {
            contenedorPagos.innerHTML = "<p class='sin-resultados'>❌ Error al conectar con el servidor de caja o procesar datos.</p>";
        }
    }
}

// Renderizador híbrido seguro (Soporta Strings y Nodos DOM)
function renderizarPagos(lista) {
    if (!contenedorPagos) return;
    
    contenedorPagos.innerHTML = "";
    
    if (lista.length === 0) {
        contenedorPagos.innerHTML = "<p class='sin-resultados'>🔍 No hay pedidos activos registrados en este período.</p>";
        return;
    }
    
    lista.forEach(pago => {
        const tarjeta = CardPagosAdmin(pago);
        
        // 🔄 SOLUCIÓN AL CRASH: Si el helper devuelve un Objeto DOM lo añade con append, si es texto usa innerHTML
        if (tarjeta instanceof Node) {
            contenedorPagos.appendChild(tarjeta);
        } else {
            contenedorPagos.innerHTML += tarjeta;
        }
    });
}

function filtrarHistorialPorPeriodo() {
    if (!selectPeriodo) return;
    const filtro = selectPeriodo.value;
    const hoy = new Date();

    const pagosFiltrados = todosLosPagos.filter(pago => {
        const fechaPago = new Date(pago.fecha + "T00:00:00"); 

        if (filtro === "diario") return fechaPago.toDateString() === hoy.toDateString();
        if (filtro === "semanal") {
            const inicioSemana = new Date(hoy);
            inicioSemana.setDate(hoy.getDate() - hoy.getDay()); 
            return fechaPago >= inicioSemana;
        }
        if (filtro === "mensual") {
            return fechaPago.getMonth() === hoy.getMonth() && fechaPago.getFullYear() === hoy.getFullYear();
        }
        return true; // "todos"
    });

    renderizarPagos(pagosFiltrados);
}

function cambiarVista(vista) {
    if (vista === 'pagos') {
        if (tabUltimosPagos) tabUltimosPagos.classList.add('active');
        if (tabMetricas) tabMetricas.classList.remove('active');
        if (panelResumenGanancias) panelResumenGanancias.style.display = 'none';
        if (contenedorPagos) contenedorPagos.style.display = 'flex';
        if (selectPeriodo) selectPeriodo.disabled = false; 
    } else {
        if (tabMetricas) tabMetricas.classList.add('active');
        if (tabUltimosPagos) tabUltimosPagos.classList.remove('active');
        if (panelResumenGanancias) panelResumenGanancias.style.display = 'grid';
        if (contenedorPagos) contenedorPagos.style.display = 'none';
        if (selectPeriodo) selectPeriodo.disabled = true; 
    }
}

// Inicialización de la ventana
document.addEventListener("DOMContentLoaded", async () => {
    await MostrarSide();
    await CargarDatosCaja();
    cambiarVista('pagos');

    document.addEventListener("click", (e) => {
        if (e.target.matches("#cerrarSesion")) {
            cerrarSesionServidor();
        }
    });

    if (tabUltimosPagos) tabUltimosPagos.addEventListener('click', () => cambiarVista('pagos'));
    if (tabMetricas) tabMetricas.addEventListener('click', () => cambiarVista('metricas'));
    if (selectPeriodo) selectPeriodo.addEventListener('change', filtrarHistorialPorPeriodo);
});