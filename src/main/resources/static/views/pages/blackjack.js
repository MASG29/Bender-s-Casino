import { element, button, stylizedButton } from "../../js/constants/element.js";

export function init() {
  document.querySelector("main").innerHTML = `
        <section class="join">
            <h2>Blackjack</h2>
        </section>
        <div id="App" class="blackjack-background"> 
            
        </div>
    `;

  const body = document.querySelector("#App");
  const ui = element("div", body, ["blackjack-ui"]);
  const start = stylizedButton(body, "Start");
  
  start.addEventListener("click", (e) => {
    body.removeChild(start);
    const hit = stylizedButton(ui, "Hit");
    const stand = stylizedButton(ui, "Stand");
    startGame();
  });

}
