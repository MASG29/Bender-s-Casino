import router from "../../router.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { element, button, stylizedButton } from "../../js/constants/element.js";
import { PAYOUTS } from "../../js/videopoker/payouts.js";
import { deal, draw } from "../../js/services/videopoker-service.js";

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
                <div class="lower-button-container">
                    <p class="amount">Bet amount: 0</p>
                </div>
            </div>
        </div>
    </div>
    `,
  );

  const buttonContainer = document.querySelector("#buttonContainer");
  const lowerButContainer = document.querySelector(".lower-button-container");
  const amountEl = document.querySelector(".amount");
  const cardSlot = `<div class="vp-card-back"></div>`;

  const bet = stylizedButton(lowerButContainer, "Bet");

  // Build 5 card slots + 5 matching Hold buttons (kept in the same index order)
  const holdButtons = [];
  for (let i = 0; i < 5; i++) {
    cardSlotContainer.insertAdjacentHTML("beforeend", cardSlot);
    const holdBtn = stylizedButton(buttonContainer, "Hold");
    holdBtn.disabled = true; // nothing to hold before a hand is dealt
    holdButtons.push(holdBtn);
  }

  let currentBetAmount = 0;
  let handId = null;
  let held = new Set();
  const playerId = sessionStorage.getItem("playerId");

  document.querySelectorAll(".bj-chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      if (handId !== null) return; // can't change bet mid-hand
      currentBetAmount += Number(chip.dataset.chip);
      amountEl.textContent = "Bet amount: " + currentBetAmount;
    });
  });

  bet.addEventListener("click", async () => {
    if (currentBetAmount <= 0) return;

    let data;
    try {
      data = await deal(playerId, currentBetAmount);
    } catch (err) {
      console.error("Deal failed:", err.message);
      return;
    }

    handId = data.handId;
    held = new Set();
    renderCards(data.cards);

    holdButtons.forEach((btn) => {
      btn.disabled = false;
      btn.classList.remove("held");
    });
    bet.disabled = true;
    document.querySelectorAll(".bj-chip").forEach((c) => (c.disabled = true));

    ensureDrawButton();
  });
  holdButtons.forEach((btn, index) => {
    btn.addEventListener("click", () => {
      if (held.has(index)) {
        held.delete(index);
        btn.classList.remove("held");
      } else {
        held.add(index);
        btn.classList.add("held");
      }
    });
  });

  function ensureDrawButton() {
    if (document.querySelector("#vp-draw")) return; // already added, don't duplicate

    const drawBtn = stylizedButton(lowerButContainer, "Draw");
    drawBtn.id = "vp-draw";

    drawBtn.addEventListener("click", async () => {
      let data;
      try {
        data = await draw(handId, Array.from(held));
      } catch (err) {
        console.error("Draw failed:", err.message);
        return;
      }

      renderCards(data.cards);
      showResult(data.category, data.payout, data.balance);

      holdButtons.forEach((btn) => (btn.disabled = true));
      drawBtn.disabled = true;
    });
  }

  function renderCards(cards) {
    const slots = cardSlotContainer.querySelectorAll(".vp-card-back");
    slots.forEach((slot, i) => {
      const card = cards[i];
      slot.innerHTML = `<img src="${card.image}" alt="${card.value} of ${card.suit}">`;
    });
  }

  function showResult(category, payout, balance) {
    amountEl.textContent =
      category + " — Payout: " + payout + " — Balance: " + balance;
  }

  return;
}
