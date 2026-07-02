// js/helpers/CardsPagos.js
export function CardPagosAdmin(pago) {
    const article = document.createElement("article");
    article.className = "tarjeta__balance"; // Reutiliza el contenedor de tarjetas estilizado
    article.style.borderLeft = pago.tipoCompra.toLowerCase() === "medida" 
        ? "6px solid var(--color-medidas)" 
        : "6px solid var(--color-secundario)";
    article.style.flexDirection = "row";
    article.style.justifyContent = "space-between";
    article.style.alignItems = "center";
    article.style.width = "100%";

    // Formateador de moneda local colombiana
    const formatoMoneda = new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
    }).format(pago.total);

    article.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 0.2rem;">
            <strong style="font-size: 1.1rem; color: var(--color-Tipografia);">Cliente: ${pago.usuario}</strong>
            <span style="font-size: 0.85rem; color: var(--color-gris-oscuro);">Método: ${pago.metodoPago} | Tipo: <b>${pago.tipoCompra}</b></span>
            <small style="color: var(--color-gris-medio); font-size: 0.8rem;">Fecha: ${pago.fecha}</small>
        </div>
        <div>
            <span style="font-size: 1.4rem; font-weight: 800; color: var(--color-Tipografia);">${formatoMoneda}</span>
        </div>
    `;
    return article;
}