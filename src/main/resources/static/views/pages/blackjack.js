import { element, button, stylizedButton } from "../../js/constants/element.js";
import { startGame } from "../../js/services/blackjack-service.js";

export function init() {
  document.querySelector("main").innerHTML = `
        <section class="join">
            <h2>Blackjack</h2>
        </section>
        <div id="App" class="blackjack-background"> 
            
        </div>
    `;

  console.log(sessionStorage.getItem("playerId"));
  const body = document.querySelector("#App");
  const ui = element("div", body, ["blackjack-ui"]);
  const start = stylizedButton(body, "Start");

  start.addEventListener("click", () => {
    body.removeChild(start);

    const form = element("form", body);
    const input = element("input", form);
    input.type = "number";
    input.name = "amount";
    const label = element("label", form);
    label.for = "amount";
    label.textContent = "Bet amount:";

    const submit = stylizedButton(form, "Bet");

    form.addEventListener("submit", (e) => {
      e.preventDefault();
      startGame(sessionStorage.getItem("playerId"), e.target.amount);
    });
    submit.addEventListener("click", (e) => {
      const hit = stylizedButton(ui, "Hit");
      const stand = stylizedButton(ui, "Stand");
    });
  });
}
