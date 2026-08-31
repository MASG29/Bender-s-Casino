import router from "../../router.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { element, button, stylizedButton } from "../../js/constants/element.js";
import { PAYOUTS, cardImageUrl } from "../../js/videopoker/payouts.js";
import { deal, draw } from "../../js/services/videopoker-service.js";
import { initTutorial } from "../../js/tutorial.js";

export function init() {
  const main = document.querySelector("main");

  initTutorial({
    game: "videopoker",
    title: "How to play Video Poker",
    body: `
      <ul>
        <li>Pick your chips to set a bet, then hit "Bet" to get 5 cards.</li>
        <li>Click "Hold" on the cards you want to keep.</li>
        <li>Hit "Draw" to replace the cards you didn't hold.</li>
        <li>Your final hand is compared to the paytable to see your payout.</li>
        <li>Hover a hand name in the paytable to see example cards.</li>
      </ul>
    `,
  });

  main.innerHTML = `
    <section class="bj">
      <h2>Video Poker</h2>
        <div class="bj-floor">
          <div id="payouts" class="payouts-columns">
${[PAYOUTS.slice(0, Math.ceil(PAYOUTS.length / 2)), PAYOUTS.slice(Math.ceil(PAYOUTS.length / 2))]
  .map(
    (half) => `
        <table class="payouts-table">
${half
  .map(
    (payout) => `
              <tr class="payout-row" data-example="${payout.example.join(",")}">
                <td>${payout.name}</td>
              </tr>
              `,
  )
  .join("")}
        </table>
        `,
  )
  .join("")}
          </div>
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
          <div id="game">
            <div id="card-slot-container">
    `;

  const cardSlotContainer = document.querySelector("#card-slot-container");

  cardSlotContainer.insertAdjacentHTML(
    "afterend",
    `
                <div id="buttonContainer">
                </div>
                <p class="vp-result"></p>
              </div>
                <div class="lower-button-container">
                    <div class="bet-panel">
                      <p class="amount">Bet amount: 0</p>
                      <button type="button" class="btn-clear-bet" id="vp-clear-bet">✕ Clear</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    `,
  );

  const payoutTooltip = document.createElement("div");
  payoutTooltip.className = "payout-tooltip hidden";
  document.body.appendChild(payoutTooltip);

  document.querySelectorAll(".payout-row").forEach((row) => {
    const example = row.dataset.example.split(",");
    row.addEventListener("mouseenter", () => {
      payoutTooltip.innerHTML = example
        .map((code) => `<img src="${cardImageUrl(code)}" alt="${code}">`)
        .join("");
      payoutTooltip.classList.remove("hidden");
    });
    row.addEventListener("mousemove", (event) => {
      payoutTooltip.style.left = event.pageX + 16 + "px";
      payoutTooltip.style.top = event.pageY + 16 + "px";
    });
    row.addEventListener("mouseleave", () => {
      payoutTooltip.classList.add("hidden");
    });
  });

  const buttonContainer = document.querySelector("#buttonContainer");
  const lowerButContainer = document.querySelector(".lower-button-container");
  const betPanel = document.querySelector(".bet-panel");
  const amountEl = document.querySelector(".amount");
  const resultEl = document.querySelector(".vp-result");
  const cardSlot = `<div class="vp-card-back"></div>`;

  const bet = stylizedButton(betPanel, "Bet");
  betPanel.insertBefore(bet, document.querySelector("#vp-clear-bet"));

  // Build 5 card slots + 5 matching Hold buttons (kept in the same index order)
  const holdButtons = [];
  for (let i = 0; i < 5; i++) {
    cardSlotContainer.insertAdjacentHTML("beforeend", cardSlot);
    const holdBtn = stylizedButton(buttonContainer, "Hold");
    holdBtn.disabled = true; // nothing to hold before a hand is dealt
    holdBtn.classList.add("hidden");
    holdButtons.push(holdBtn);
  }

  let currentBetAmount = 0;
  let lastBetAmount = 0;
  let handId = null;
  let held = new Set();
  const playerId = sessionStorage.getItem("playerId");
  const cardSlots = cardSlotContainer.querySelectorAll(".vp-card-back");

  const rebetBtn = stylizedButton(betPanel, "Bet Again");
  const newBetBtn = stylizedButton(betPanel, "New Bet");
  rebetBtn.classList.add("hidden");
  newBetBtn.classList.add("hidden");

  document.querySelectorAll(".bj-chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      if (handId !== null) return; // can't change bet mid-hand
      currentBetAmount += Number(chip.dataset.chip);
      amountEl.textContent = "Bet amount: " + currentBetAmount;
    });
  });

  document.querySelector("#vp-clear-bet").addEventListener("click", () => {
    if (handId !== null) return; // can't change bet mid-hand
    currentBetAmount = 0;
    amountEl.textContent = "Bet amount: " + currentBetAmount;
  });

  async function startHand(betAmount) {
    let data;
    try {
      data = await deal(playerId, betAmount);
    } catch (err) {
      console.error("Deal failed:", err.message);
      return;
    }

    lastBetAmount = betAmount;
    handId = data.handId;
    held = new Set();
    renderCards(data.cards);
    resultEl.textContent = "";
    resultEl.classList.remove("win", "lose");

    holdButtons.forEach((btn) => {
      btn.disabled = false;
      btn.classList.remove("held", "hidden");
    });
    cardSlots.forEach((slot) => slot.classList.remove("held"));
    bet.classList.add("hidden");
    rebetBtn.classList.add("hidden");
    newBetBtn.classList.add("hidden");
    document.querySelectorAll(".bj-chip").forEach((c) => (c.disabled = true));

    ensureDrawButton();
  }

  bet.addEventListener("click", () => {
    if (currentBetAmount <= 0) return;
    startHand(currentBetAmount);
  });

  rebetBtn.addEventListener("click", () => {
    currentBetAmount = lastBetAmount;
    amountEl.textContent = "Bet amount: " + currentBetAmount;
    startHand(currentBetAmount);
  });

  newBetBtn.addEventListener("click", () => {
    currentBetAmount = 0;
    amountEl.textContent = "Bet amount: " + currentBetAmount;
    rebetBtn.classList.add("hidden");
    newBetBtn.classList.add("hidden");
    bet.classList.remove("hidden");
    bet.disabled = false;
    document.querySelectorAll(".bj-chip").forEach((c) => (c.disabled = false));
  });

  holdButtons.forEach((btn, index) => {
    btn.addEventListener("click", () => {
      if (held.has(index)) {
        held.delete(index);
        btn.classList.remove("held");
        cardSlots[index].classList.remove("held");
      } else {
        held.add(index);
        btn.classList.add("held");
        cardSlots[index].classList.add("held");
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

      holdButtons.forEach((btn) => {
        btn.disabled = true;
        btn.classList.remove("held");
        btn.classList.add("hidden");
      });
      cardSlots.forEach((slot) => slot.classList.remove("held"));
      drawBtn.remove();

      handId = null;
      held = new Set();
      rebetBtn.classList.remove("hidden");
      newBetBtn.classList.remove("hidden");
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
    resultEl.textContent = payout > 0 ? `${category} — Won ${payout}` : `${category} — No win`;
    resultEl.classList.toggle("win", payout > 0);
    resultEl.classList.toggle("lose", payout <= 0);
    amountEl.textContent = "Balance: " + balance;
  }

  return;
}
