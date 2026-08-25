import { element, button, stylizedButton } from "../../js/constants/element.js";
import {
  getState,
  startGame,
  playerHit,
  playerStand,
} from "../../js/services/blackjack-service.js";

const CHIPS = [
  { value: 1, image: "/assets/Coins/1dollar.coin.png" },
  { value: 5, image: "/assets/Coins/5dollarcoin.png" },
  { value: 10, image: "/assets/Coins/10dollarcoin.png" },
  { value: 25, image: "/assets/Coins/25dollarcoin.png" },
  { value: 50, image: "/assets/Coins/50dollarcoin.png" },
  { value: 100, image: "/assets/Coins/100dollarcoin.png" },
];

function tableMarkup() {
  return `
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

                <div id="table" class="bj-table">
                    <div class="bj-deck" id="bj-deck">
                        <div class="bj-deck-card"></div>
                        <div class="bj-deck-card"></div>
                        <div class="bj-deck-card"></div>
                        <div class="bj-deck-card"></div>
                        <div class="bj-deck-card"></div>
                    </div>

                    <div id="dealer" class="bj-seat bj-dealer">
                        <p class="bj-seat-label">Dealer</p>
                        <div class="bj-hand" id="dealer-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-score" id="dealer-score">—</p>
                    </div>

          <p id="ending"></p>
          <form>
            <p id="bet-amount" name="amount">
            Bet amount: 0
            </p>
            <button type="submit">
              <span class="button_top">Start</span>
            </button> 
          </form>

          <div id="player" class="bj-seat bj-player">
            <p class="bj-score" id="player-score">—</p>
            <div class="bj-hand" id="player-hand">
              <div class="bj-slot"></div>
              <div class="bj-slot"></div>
            </div>
            <p class="bj-seat-label">You</p>
          </div>
        </div>
      </div>

      <div id="ui" class="blackjack-ui">
        <button type="button" id="bj-hit"><span class="button_top">Hit</span></button>
        <button type="button" id="bj-stand"><span class="button_top">Stand</span></button>
      </div>
    </section>
  `;
}

export function init() {
  let amount = 0;
  const main = document.querySelector("main");
  const start = stylizedButton(main, "Start");

  start.addEventListener("click", async () => {
    main.removeChild(start);
    await startRound();
  });

  // One round of the game: render markup, wire everything, play until FINISHED.
  async function startRound() {
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

    function renderPlayerCards() {
      playerCardsContainer.innerHTML = "";
      gameState.playerHand.cards.forEach((c) => {
        const cardEl = element("div", playerCardsContainer);
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

    function chips() {
      document.querySelectorAll(".bj-chip").forEach((e) => {
        e.addEventListener("click", () => {
          console.log(e.dataset.chip);
          amount += parseInt(e.dataset.chip);
          console.log(amount);
          formContainer.textContent = "Bet amount: " + amount;
        });
      });
    }

    function displayScores() {
      playerScoreEl.textContent = "Score: " + gameState.playerHand.value;
      dealerScoreEl.textContent = "Score: " + gameState.dealerHand.value;
    }

    function setControlsEnabled(enabled) {
      hit.disabled = !enabled;
      stand.disabled = !enabled;
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
      table.removeChild(formContainer);
      renderPlayerCards();
      renderDealerCards();
      displayScores();
    } else {
      setControlsEnabled(false); // no hitting/standing before a bet exists

      chips();

      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        table.removeChild(form);

        try {
          gameState = await startGame(
            sessionStorage.getItem("playerId"),
            e.target.amount,
          );
        } catch (err) {
          console.error("Failed to start game:", err.message);
          return;
        }

        renderPlayerCards();
        renderDealerCards();
        displayScores();
        setControlsEnabled(true);
      });
    }
  }
}
