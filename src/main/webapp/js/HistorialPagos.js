// js/HistorialPagos.js
import { MostrarSide } from "../helpers/RelizarPeticion.js";
import { cerrarSesionServidor } from "../helpers/CerrarSesion.js";
import { CardPagosAdmin } from "../helpers/CardsPagosAdmin.js";

// Selectores de UI
const tabUltimosPagos = document.getElementById('tab-ultimos-pagos');
const tabMetricas = document.getElementById('tab-metricas');
const panelResumenGanancias = document.getElementById('resumenGanancias');
const selectPeriodo = document.getElementById('periodo-caja');
const contenedorPagos = document.getElementById('contenedorPagos');

// Elementos de los KPIs
const montoDiario = document.getElementById('monto-diario');
const montoSemanal = document.getElementById('monto-semanal');
const montoMensual = document.getElementById('monto-mensual');

let todosLosPagos = []; // Respaldo para filtros en tiempo real

const formatearDinero = (valor) => {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
};

// Carga principal unificada de datos
async function CargarDatosCaja() {
    try {
        const respuesta = await fetch("../ObtenerHistorialPagos");
        if (!respuesta.ok) throw new Error("Error obteniendo el historial de transacciones");

        const datos = await respuesta.json();

        // 1. Inyectar valores numéricos en los cuadros de KPI
        montoDiario.textContent = formatearDinero(datos.metricas.diario);
        montoSemanal.textContent = formatearDinero(datos.metricas.semanal);
        montoMensual.textContent = formatearDinero(datos.metricas.mensual);

        // 2. Guardar listado completo y renderizar inicialización
        todosLosPagos = datos.listaPagos;
        renderizarPagos(todosLosPagos);

    } catch (error) {
        console.error(error);
        contenedorPagos.innerHTML = "<p class='sin-resultados'>❌ Error al conectar con el servidor de caja.</p>";
    }
}

function renderizarPagos(lista) {
    contenedorPagos.innerHTML = "";
    if (lista.length === 0) {
        contenedorPagos.innerHTML = "<p class='sin-resultados'>🔍 No hay transacciones registradas en este período.</p>";
        return;
    }
    lista.forEach(pago => {
        contenedorPagos.appendChild(CardPagosAdmin(pago));
    });
}

function filtrarHistorialPorPeriodo() {
    const filtro = selectPeriodo.value;
    const hoy = new Date();

    const pagosFiltrados = todosLosPagos.filter(pago => {
        const fechaPago = new Date(pago.fecha + "T00:00:00"); // Evita desajuste de zona horaria

        if (filtro === "diario") {
            return fechaPago.toDateString() === hoy.toDateString();
        }
        if (filtro === "semanal") {
            const inicioSemana = new Date(hoy);
            inicioSemana.setDate(hoy.getDate() - hoy.getDay()); // Domingo o Lunes según configuración
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
        tabUltimosPagos.classList.add('active');
        tabMetricas.classList.remove('active');
        panelResumenGanancias.style.display = 'none';
        contenedorPagos.style.display = 'flex';
        if(selectPeriodo) selectPeriodo.disabled = false; // El combo interactúa con la lista
    } else {
        tabMetricas.classList.add('active');
        tabUltimosPagos.classList.remove('active');
        panelResumenGanancias.style.display = 'grid';
        contenedorPagos.style.display = 'none';
        if(selectPeriodo) selectPeriodo.disabled = true; // Kpis estáticos globales
    }
}

// Control del Ciclo de Vida del DOM
document.addEventListener("DOMContentLoaded", async () => {
    // Inicializar la sidebar de administrador
    await MostrarSide();

    // Invocar el canal asíncrono unificado
    await CargarDatosCaja();

    // Forzar vista por defecto limpia (Lista de pagos visible, KPIs ocultos hasta presionar la pestaña)
    cambiarVista('pagos');

    // Escuchadores de eventos
    if (tabUltimosPagos && tabMetricas) {
        tabUltimosPagos.addEventListener('click', () => cambiarVista('pagos'));
        tabMetricas.addEventListener('click', () => cambiarVista('metricas'));
    }

    if (selectPeriodo) {
        selectPeriodo.addEventListener('change', filtrarHistorialPorPeriodo);
    }

    const btnCerrar = document.querySelector("#cerrarSesion");
    if (btnCerrar) {
        btnCerrar.addEventListener("click", () => cerrarSesionServidor());
    }
});