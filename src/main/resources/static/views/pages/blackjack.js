import { element, button, stylizedButton } from "../../js/constants/element.js";
import { getState, startGame, playerHit, playerStand } from "../../js/services/blackjack-service.js";

const CHIPS = [
    { value: 1, image: "/assets/1dollar.coin.png" },
    { value: 5, image: "/assets/5dollarcoin.png" },
    { value: 10, image: "/assets/10dollarcoin.png" },
    { value: 25, image: "/assets/25dollarcoin.png" },
    { value: 50, image: "/assets/50dollarcoin.png" },
    { value: 100, image: "/assets/100dollarcoin.png" },
];

export function init() {

  console.log(sessionStorage.getItem("playerId"));
  const main = document.querySelector("main");
  const ui = element("div", main, ["blackjack-ui"]);

  const start = stylizedButton(main, "Start");

  start.addEventListener("click", async () => {
    main.removeChild(start);

    document.querySelector("main").innerHTML = `
        <section class="bj">
            <h2>Blackjack</h2>

            <div class="bj-floor">
                <div class="bj-chips">
                    ${CHIPS.map(chip => `
                        <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                            <img src="${chip.image}" alt="${chip.value} dollar chip">
                        </button>
                    `).join("")}
                </div>

                <div id="table" class="bj-table">
                    <div id="dealer" class="bj-seat bj-dealer">
                        <p class="bj-seat-label">Dealer</p>
                        <div class="bj-hand" id="dealer-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-score" id="dealer-score">—</p>
                    </div>

                    <div id="form" >
                    <form> 
                    <label for = "amount">Bet amount:</label>
                    <input type="number" name="amount">
                    </form> 
                    </div>

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

    const ui = document.querySelector("#ui");
    const hit = document.querySelector("#bj-hit");
    const stand = document.querySelector("#bj-stand");
    const table = document.querySelector("#table")
    const playerTable = document.querySelector("#player");
    const dealerTable = document.querySelector("#dealer");
    const formContainer = document.querySelector("#form");
    const form = document.querySelector("form");   
    let gameState = await getState(sessionStorage.getItem("playerId"));


    //debug
    console.log(gameState);
    const playerCardsContainer = element("div", playerTable)
    const dealerCardsContainer = element("div", dealerTable)

    function playerCards() {
        gameState.playerHand.cards.forEach(e => {
            const cardContainer = element("div", playerCardsContainer);
            const p = element("p", cardContainer);
            p.textContent = e.value + " of " + e.suit;
        })
    }

    function dealerCards() {
        gameState.dealerHand.cards.forEach(e => {
            const cardContainer = element("div", dealerCardsContainer);
            const p = element("p", cardContainer);
            p.textContent = e.value + " of " + e.suit;
        })

        
    }

    function displayScores() {
        const playerScore = element("p", playerTable);
        const dealerScore = element("p", dealerTable);
        playerScore.textContent = "Score: " + gameState.playerHand.value;
        dealerScore.textContent = "Score: " + gameState.dealerHand.value;

    }

    function updatePlayerCards() {
        playerCardsContainer.innerHTML = "";
        playerCards();
    }

    function updateDealerCards() {
        dealerCardsContainer.innerHTML = "";
        dealerCards();
    }

    function controls() {
        hit.addEventListener("click", async () => {
            gameState = await playerHit(sessionStorage.getItem("playerId"));
            console.log(gameState);
            updatePlayerCards();
        })
        stand.addEventListener("click", async () => {
            gameState = await playerStand(sessionStorage.getItem("playerId"));
            console.log(gameState);
            updateDealerCards();
        })
    }

    if (gameState.status == "PLAYER_TURN") {
        table.removeChild(formContainer);
        playerCards();
        dealerCards();
        displayScores();
    }

    else { 

    form.addEventListener("submit", async (e) => {
      table.removeChild(formContainer);
      e.preventDefault();
    
      gameState = await startGame(sessionStorage.getItem("playerId"), e.target.amount.value);

      playerCards();
      dealerCards();
      displayScores();

    })
    }
    controls();
    });
}
