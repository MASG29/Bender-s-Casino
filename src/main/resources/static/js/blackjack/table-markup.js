import { CHIPS } from "./chips.js";

export function tableMarkup() {
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
            <button type="button" class="btn-clear-bet" id="bj-clear-bet">✕ Clear</button>
            <button type="submit">
              <span class="button_top">Start</span>
            </button>
          </form>

          <div id="player" class="bj-seat bj-player">
            <p class="bj-score" id="player-score">—</p>
            <div class="bj-hand" id="player-hand">
              <div id="player-slot-1" class="bj-slot"></div>
              <div id="player-slot-2" class="bj-slot"></div>
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
