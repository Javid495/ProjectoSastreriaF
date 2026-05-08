import { llamarComponente } from "./CompHtml.js";

const header = document.querySelector(".header");
const footer = document.querySelector(".footer");

llamarComponente(header, "/src/main/webapp/componentesWeb/header.html");
llamarComponente(footer, "/src/main/webapp/componentesWeb/footer.html");