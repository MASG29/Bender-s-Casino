import {
  element,
  button,
  stylizedButton,
  move,
} from "../../js/constants/element.js";

import {
  getState,
  startGame,
  playerHit,
  playerStand,
} from "../../js/services/blackjack-service.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { tableMarkup } from "../../js/blackjack/table-markup.js";

export async function startRound() {
  let amount = 0;
  document.querySelector("main").innerHTML = tableMarkup();

  const hit = document.querySelector("#bj-hit");
  const stand = document.querySelector("#bj-stand");
  const table = document.querySelector("#table");
  const playerTable = document.querySelector("#player");
  const dealerTable = document.querySelector("#dealer");
  const form = document.querySelector("form");
  const formContainer = document.querySelector("#bet-amount");
  const playerScoreEl = document.querySelector("#player-score");
  const dealerScoreEl = document.querySelector("#dealer-score");
  const endingEl = document.querySelector("#ending");

  const playerSlot1 = document.querySelector("#player-slot-1");
  const playerSlot2 = document.querySelector("#player-slot-2");

  const playerCardsContainer = element("div", playerTable);
  const dealerCardsContainer = element("div", dealerTable);

  // FIX: getState throws (404 GAME_NOT_FOUND) for a fresh player with no
  // active game. That used to crash startRound() before the form's submit
  // listener was attached, so the browser fell back to a native form
  // submit -> page reload. Now it falls back to a NO_GAME state instead.
  let gameState;
  try {
    gameState = await getState(sessionStorage.getItem("playerId"));
  } catch (err) {
    console.log("No active game found, starting fresh:", err.message);
    gameState = { status: "NO_GAME" };
  }

  function setControlsEnabled(enabled) {
    hit.disabled = !enabled;
    stand.disabled = !enabled;
  }

  function displayScores() {
    playerScoreEl.textContent = "Score: " + gameState.playerHand.value;
    dealerScoreEl.textContent = "Score: " + gameState.dealerHand.value;
  }

  function updateChips() {
    document.querySelectorAll(".bj-chip").forEach((e) => {
      e.addEventListener("click", () => {
        amount += parseInt(e.dataset.chip);
        formContainer.textContent = "Bet amount: " + amount;
      });
    });
  }

  function renderPlayerCards() {
    playerCardsContainer.innerHTML = "";
    gameState.playerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card

      console.log(playerSlot1.getBoundingClientRect()); //debug
      move(
        cardEl,
        playerSlot1.getBoundingClientRect(),
        cardEl.getBoundingClientRect(),
      );
      element("p", cardEl).textContent = c.value + " of " + c.suit;
    });
  }

  function renderDealerCards() {
    dealerCardsContainer.innerHTML = "";
    gameState.dealerHand.cards.forEach((c) => {
      const cardEl = element("div", dealerCardsContainer);
      element("p", cardEl).textContent = c.value + " of " + c.suit;
    });
  }

  function checkForEnd() {
    if (gameState.status !== "FINISHED") return false;

    setControlsEnabled(false);

    switch (gameState.outcome) {
      case "PLAYER_WIN":
        endingEl.textContent = "Player wins!";
        break;
      case "DEALER_WIN":
        endingEl.textContent = "Dealer wins!";
        break;
      default:
        endingEl.textContent = "Round over.";
    }

    const playAgain = stylizedButton(endingEl, "Play again");
    playAgain.addEventListener("click", () => startRound());

    return true;
  }

  hit.addEventListener("click", async () => {
    if (gameState.status !== "PLAYER_TURN") return;
    try {
      gameState = await playerHit(sessionStorage.getItem("playerId"));
    } catch (err) {
      console.error("Hit failed:", err.message);
      return;
    }
    renderPlayerCards();
    displayScores();
    checkForEnd();
  });

  stand.addEventListener("click", async () => {
    if (gameState.status !== "PLAYER_TURN") return;
    try {
      gameState = await playerStand(sessionStorage.getItem("playerId"));
    } catch (err) {
      console.error("Stand failed:", err.message);
      return;
    }
    renderDealerCards();
    displayScores();
    checkForEnd();
  });

  if (gameState.status === "PLAYER_TURN") {
    table.removeChild(form);
    renderPlayerCards();
    renderDealerCards();
    displayScores();
  } else {
    setControlsEnabled(false); // no hitting/standing before a bet exists

    updateChips();

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (amount <= 0) {
        formContainer.textContent = "Pick at least one chip before proceeding!";
        return;
      }
      table.removeChild(form);

      try {
        gameState = await startGame(sessionStorage.getItem("playerId"), amount);
      } catch (err) {
        console.error("Failed to start game:", err.message);
        return;
      }

      console.log(gameState);
      renderPlayerCards();
      renderDealerCards();
      displayScores();
      setControlsEnabled(true);
    });
  }
}
