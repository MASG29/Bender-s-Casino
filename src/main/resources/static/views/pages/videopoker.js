import router from "../../router.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { element, button, stylizedButton } from "../../js/constants/element.js";
import { PAYOUTS } from "../../js/videopoker/payouts.js";

export function init() {
  const main = document.querySelector("main");

  main.innerHTML = `

    <section class="bj">
      <h2>Video Poker</h2>
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
    <div class="vp-table">
      <table id="payouts" class="payouts-table">
            ${PAYOUTS.map(
              (payout) => `
              <tr>
                <td>${payout.name}</td>
                ${payout.payout.map((el) => `<td>${el}</td>`).join("")}
              </tr>
              `,
            ).join("")}
    </table>
          <div id="game">
            <div id="card-slot-container">
    `;

  const cardSlotContainer = document.querySelector("#card-slot-container");

  cardSlotContainer.insertAdjacentHTML(
    "afterend",
    `
                <div id="buttonContainer">
                </div>
              </div>
                <div class = "lower-button-container">
                    <p class="amount">Bet amount: 0</p>
                </div>
            </div>
        </div>
    </div>
    `,
  );

  const buttonContainer = document.querySelector("#buttonContainer");
  const lowerButContainer = document.querySelector(".lower-button-container");
  const cardSlot = `<div class="bj-slot"></div>`;

  const bet = stylizedButton(lowerButContainer, "Bet");

  for (let i = 0; i < 5; i++) {
    cardSlotContainer.insertAdjacentHTML("beforeend", cardSlot);
    stylizedButton(buttonContainer, "Hold");
  }

  return;
}

function getOptions() {
  const buttonContainer = document.querySelector("#buttonContainer");
  const pokerHand = 5;

  for (let val of pokerHand) {
  }
}
