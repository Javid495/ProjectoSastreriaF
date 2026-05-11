import { llamarComponente } from "./CompHtml.js";


export function aparecerCont(){
    llamarComponente(".header", "./componentesWeb/header.html");
    llamarComponente(".footer", "./componentesWeb/footer.html");
}
