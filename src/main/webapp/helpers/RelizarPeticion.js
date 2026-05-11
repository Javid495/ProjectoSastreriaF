import { llamarComponente } from "./CompHtml.js";


const header = document.querySelector(".header");
const footer = document.querySelector(".footer");

llamarComponente(header, "./componentesWeb/header.html");
llamarComponente(footer, "./componentesWeb/footer.html");