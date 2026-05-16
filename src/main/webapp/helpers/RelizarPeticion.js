import { llamarComponente } from "./CompHtml.js";


export async function aparecerCont(ruta){
    console.log(ruta);
    
    await llamarComponente(".header", `${ruta}componentesWeb/header.html`);
    await llamarComponente(".footer", `${ruta}componentesWeb/footer.html`);

    const links = document.querySelectorAll(".header__item");
    links.forEach(link => {
        const hrefOriginal = link.getAttribute("href");
        // Si no es una ruta absoluta, le ponemos el prefijo (./ o ../)
        if (!hrefOriginal.startsWith("/") && !hrefOriginal.startsWith("http")) {
            link.href = `${ruta}${hrefOriginal}`;
        }
    });

    const images = document.querySelectorAll(".icon__image");
    images.forEach(img => {
        const srcOriginal = img.getAttribute("src");
        // Si no es una ruta absoluta, le ponemos el prefijo (./ o ../)
        if (srcOriginal && srcOriginal.startsWith("images/")) {
            img.setAttribute("src", ruta + srcOriginal);
        }
    });

    // const logo = document.querySelector("#logo");

    //     const logoRuta = logo.getAttribute("src");

    //     // Si no es una ruta absoluta, le ponemos el prefijo (./ o ../)
    //     if (logoRuta && logoRuta.startsWith("images/")) {
    //         logo.setAttribute("src", ruta + logoRuta);
    //     }

}

export async function MostrarSide(){

    llamarComponente(".sidebar","../componentesWeb/aside.html")

}
