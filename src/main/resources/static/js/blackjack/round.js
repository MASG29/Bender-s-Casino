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
import { initTutorial } from "../../js/tutorial.js";

// "Play again" calls startRound() again without a full page reload, which
// would otherwise stack a new resize listener (closing over that round's
// now-detached hand elements) on top of the previous one every round.
let activeResizeHandler = null;

export async function startRound() {
  let amount = 0;
  let playerCards = 0;
  let dealerCards = 0;
  document.querySelector("main").innerHTML = tableMarkup();

  initTutorial({
    game: "blackjack",
    title: "How to play Blackjack",
    body: `
      <ul>
        <li>Pick your chips to set a bet, then hit "Start".</li>
        <li>"Hit" draws another card; "Stand" ends your turn.</li>
        <li>Get closer to 21 than the dealer without going over.</li>
        <li>A 21 with your first two cards is a Blackjack and pays extra.</li>
        <li>The dealer must keep drawing until reaching 17 or more.</li>
      </ul>
    `,
  });

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
  const jokeEl = document.querySelector("#bj-joke");

  function displayJoke() {
    jokeEl.textContent = gameState.benderJoke ? "Bender: " + gameState.benderJoke : "";
  }

  const playerSlot1 = document.querySelector("#player-slot-1");
  const playerSlot2 = document.querySelector("#player-slot-2");
  const playerHandEl = document.querySelector("#player-hand");
  const dealerHandEl = document.querySelector("#dealer-hand");
  const deckEl = document.querySelector("#bj-deck");

  // Returns the pixel offset (for move()) that lands a card exactly on the
  // slot at `cardIndex` within `handEl`. Measured from the deck, not from
  // the card itself: `.bj-card` has a `transition: transform`, so reading a
  // dealt card's own getBoundingClientRect() right after resetting its
  // transform can still report the pre-reset (mid-transition) position —
  // the deck never gets a transform, so it's a stable reference for both
  // the initial deal and any later reposition (e.g. on resize). Cards
  // beyond the two rendered .bj-slot elements (i.e. hits) fan out to the
  // right of the last slot, spaced the same as the CSS `.bj-hand` gap, so
  // they never land on the score/label above or below the hand.
  function cardTargetOffset(handEl, cardIndex) {
    const startRect = deckEl.getBoundingClientRect();
    const slots = handEl.querySelectorAll(".bj-slot");
    const lastSlot = slots[slots.length - 1];
    const targetSlot = slots[cardIndex] || lastSlot;
    const slotRect = targetSlot.getBoundingClientRect();
    const gap = parseFloat(getComputedStyle(handEl).gap) || 12;
    const extra =
      cardIndex >= slots.length ? (cardIndex - slots.length + 1) * (slotRect.width + gap) : 0;

    return {
      left: slotRect.left - startRect.left + extra,
      top: slotRect.top - startRect.top,
    };
  }

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

    document.querySelector("#bj-clear-bet").addEventListener("click", () => {
      amount = 0;
      formContainer.textContent = "Bet amount: " + amount;
    });
  }

  function placeCard(cardEl, hand, handEl, cardIndex) {
    cardEl.dataset.hand = hand;
    cardEl.dataset.cardIndex = cardIndex;
    move(cardEl, cardTargetOffset(handEl, cardIndex), 0);
  }

  // Card positions are computed once, at deal time, from each .bj-slot's
  // real position. A window resize can change the table/slot layout (see
  // the @media rules in table.css) without moving already-placed cards, so
  // re-snap every dealt card to its slot whenever the window resizes.
  function repositionDealtCards() {
    document.querySelectorAll(".bj-card[data-hand]").forEach((cardEl) => {
      const handEl = cardEl.dataset.hand === "player" ? playerHandEl : dealerHandEl;
      move(cardEl, cardTargetOffset(handEl, Number(cardEl.dataset.cardIndex)), 0);
    });
  }
  if (activeResizeHandler) window.removeEventListener("resize", activeResizeHandler);
  activeResizeHandler = repositionDealtCards;
  window.addEventListener("resize", activeResizeHandler);

  function renderPlayerCards() {
    playerCardsContainer.innerHTML = "";

    gameState.playerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      playerCards++;
      placeCard(cardEl, "player", playerHandEl, playerCards - 1);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }

  function addPlayerCard() {
    const cardEl = element("div", table, ["bj-card"]);
    playerCards++;
    placeCard(cardEl, "player", playerHandEl, playerCards - 1);
    const lastCard = gameState.playerHand.cards[gameState.playerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
  }

  function renderDealerCards() {
    playerCardsContainer.innerHTML = "";

    gameState.dealerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      dealerCards++;
      placeCard(cardEl, "dealer", dealerHandEl, dealerCards - 1);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }
  function addDealerCard() {
    const cardEl = element("div", table, ["bj-card"]);
    dealerCards++;
    placeCard(cardEl, "dealer", dealerHandEl, dealerCards - 1);
    const lastCard = gameState.dealerHand.cards[gameState.dealerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
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
    addPlayerCard();
    displayScores();
    displayJoke();
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
    addDealerCard();
    displayScores();
    displayJoke();
    checkForEnd();
  });

  if (gameState.status === "PLAYER_TURN") {
    form.remove();
    renderPlayerCards();
    renderDealerCards();
    displayScores();
    displayJoke();
  } else {
    setControlsEnabled(false); // no hitting/standing before a bet exists

    updateChips();

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (amount <= 0) {
        formContainer.textContent = "Pick at least one chip before proceeding!";
        return;
      }
      form.remove();

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
      displayJoke();
      setControlsEnabled(true);
    });
  }
}
