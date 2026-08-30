import router from "../../router.js";
import { CHIPS } from "../../js/blackjack/chips.js";

export function init() {
  const main = document.querySelector("main");

  main.innerHTML = `

    <section class="bj">
      <h2>Blackjack</h2>
        <div class="bj-floor">
          <div id="chips" class="bj-chips">
            ${CHIPS.map(
              (chip) => `
              <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                <img src="${chip.image}" alt="${chip.value} dollar chip">
              </button>
            `,
            ).join("")}
    </div>

    <div id="arcade">
        <div id="table">
          <div class="bj-deck" id="bj-deck">
            <div class="bj-deck-card"></div>
            <div class="bj-deck-card"></div>
            <div class="bj-deck-card"></div>
            <div class="bj-deck-card"></div>
            <div class="bj-deck-card"></div>
          </div>
          <div id= "game">
            <div id="card-slot-container">
    `;

  const cardSlotContainer = document.querySelector("#card-slot-container");

  const cardSlot = `<div class="card-slot">
                    </div>`;
  for (let i = 0; i < 5; i++) {
    cardSlotContainer.insertAdjacentHTML("beforeend", cardSlot);
  }

  cardSlotContainer.insertAdjacentHTML(
    "afterend",
    `
                  <div id="buttonContainer">
                </div>
              </div>
            </div>
        </div>
    </div>
    `,
  );
  return;
}
