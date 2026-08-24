<<<<<<< HEAD
import { element, button, stylizedButton } from "../../js/constants/element.js";
import { startGame } from "../../js/services/blackjack-service.js";

export function init() {
  document.querySelector("main").innerHTML = `
        <section class="join">
=======
const CHIPS = [
    { value: 1, image: "/assets/1dollar.coin.png" },
    { value: 5, image: "/assets/5dollarcoin.png" },
    { value: 10, image: "/assets/10dollarcoin.png" },
    { value: 25, image: "/assets/25dollarcoin.png" },
    { value: 50, image: "/assets/50dollarcoin.png" },
    { value: 100, image: "/assets/100dollarcoin.png" },
];

export function init() {
    document.querySelector("main").innerHTML = `
        <section class="bj">
>>>>>>> 1c8ee0fd564a4c0f71f3f2eb11660ed53e4813ef
            <h2>Blackjack</h2>

            <div class="bj-floor">
                <div class="bj-chips">
                    ${CHIPS.map(chip => `
                        <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                            <img src="${chip.image}" alt="${chip.value} dollar chip">
                        </button>
                    `).join("")}
                </div>

                <div class="bj-table">
                    <div class="bj-seat bj-dealer">
                        <p class="bj-seat-label">Dealer</p>
                        <div class="bj-hand" id="dealer-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-score" id="dealer-score">—</p>
                    </div>

                    <div class="bj-seat bj-player">
                        <p class="bj-score" id="player-score">—</p>
                        <div class="bj-hand" id="player-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-seat-label">You</p>
                    </div>
                </div>
            </div>

            <div class="blackjack-ui">
                <button type="button" id="bj-hit"><span class="button_top">Hit</span></button>
                <button type="button" id="bj-stand"><span class="button_top">Stand</span></button>
            </div>
        </section>
        <div id="App" class="blackjack-background"> 
            
        </div>
    `;
<<<<<<< HEAD

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
=======
>>>>>>> 1c8ee0fd564a4c0f71f3f2eb11660ed53e4813ef
}
