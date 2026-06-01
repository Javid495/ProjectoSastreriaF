import { llamarComponente } from "./CompHtml.js";


export async function aparecerCont(ruta){
    console.log(ruta);
    
    await llamarComponente(".header", `${ruta}componentesWeb/header.html`);
    await llamarComponente(".footer", `${ruta}componentesWeb/footer.html`);
    await llamarComponente("#btnCompra", `${ruta}componentesWeb/BotonCarrito.html`);

    const links = document.querySelectorAll(".header__item");
    const inicioSesion = document.querySelector(".header__login");
    const images = document.querySelectorAll(".icon__image");

    // Verificar rutas de navegacion 
    links.forEach(link => {
        const hrefOriginal = link.getAttribute("href");
        // Si no es una ruta absoluta, le ponemos el prefijo (./ o ../)
        if (!hrefOriginal.startsWith("/") && !hrefOriginal.startsWith("http")) {
            link.href = `${ruta}${hrefOriginal}`;
        }
    });

    // Para verificar las rutas de la imagenes del footer
    images.forEach(img => {
        const srcOriginal = img.getAttribute("src");
        // Si no es una ruta absoluta, le ponemos el prefijo (./ o ../)
        if (srcOriginal && srcOriginal.startsWith("images/")) {
            img.setAttribute("src", ruta + srcOriginal);
        }
    });

    
    //Verificar Ruta del carrito
    // const Carrito = document.querySelector(".filtrados__cart");
    // const linkCarrito = Carrito.getAttribute("href");

    // if (!linkCarrito.startsWith("/") && !linkCarrito.startsWith("http")) {
    //     Carrito.href = `${ruta}${linkCarrito}`;
    // }

    // 1. Capturamos los elementos de la interfaz
    const logo = document.querySelector("#logo");
    const logoCarrito = document.querySelector(".cart__icon");

    // 2. Verificación del Logo Principal
    // Solo si el logo EXISTE en la página actual, ejecutamos su lógica
    if (logo) { 
    const logoRuta = logo.getAttribute("src");
    
    if (logoRuta && logoRuta.startsWith("images/")) {
        logo.setAttribute("src", ruta + logoRuta);
    }
    } 
    else {    
        console.log("El elemento #logo no existe en esta vista, se ignora.");
    }

    // 3. Verificación del Logo del Carrito
    // Descomentamos y protegemos igual: si no existe, JavaScript simplemente pasa de largo
    if (logoCarrito) {  
    const rutaLogoCart = logoCarrito.getAttribute("src");
    
    if (rutaLogoCart && rutaLogoCart.startsWith("images/")) {
        logoCarrito.setAttribute("src", ruta + rutaLogoCart);
    }} 
    else {
        console.log("El elemento .cart__icon no existe en esta vista, se ignora.");
    }


}

export async function MostrarSide(){

    llamarComponente(".sidebar","../componentesWeb/aside.html")

}
