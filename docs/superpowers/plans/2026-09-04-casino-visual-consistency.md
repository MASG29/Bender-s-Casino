# Unified bet rail + Blackjack card-position fix — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Group each game's bet-amount/Clear/action-button with its chip rail in one visual panel, uniformize Roulette's chip UI with the other games, and fix Blackjack cards visually covering the score during the deal/hit animation.

**Architecture:** Pure frontend (vanilla JS + CSS, no build step). Reuses the existing `#chips.bj-chips` flex-column panel as the single "bet rail" container across Blackjack, Video Poker, Slots, and Roulette by appending each game's existing bet controls into it instead of leaving them in the felt/machine area. Blackjack's card-flight math switches from hardcoded pixel offsets to positions read live from the real `.bj-slot` elements.

**Tech Stack:** Vanilla JS modules, plain CSS (`styles/*.css`), Spring Boot serves static files unchanged.

**Spec:** `docs/superpowers/specs/2026-09-04-casino-visual-consistency-design.md`

## Global Constraints

- No new dependencies, no build tooling, no new color palette or font — reuse existing tokens (`--gold`, `--panel`, `--steel-dark`, IBM Plex Mono).
- No backend changes.
- No changes to Peixinho, Home, Lobby, or Profile markup/CSS (already consistent, out of scope per spec).
- No frontend test suite exists in this repo — verification is a manual pass in the running app (`./mvnw spring-boot:run`, then the app in a browser) after each task, not an automated test run.
- Commit messages: English, no AI/tool mentions (repo convention).

---

### Task 1: Relocate `.btn-clear-bet` into the shared stylesheet

**Files:**
- Modify: `src/main/resources/static/styles/roulette.css` (remove `.btn-clear-bet` and `.btn-clear-bet:hover`)
- Modify: `src/main/resources/static/styles/style.css` (add the same rules)

**Why first:** `.btn-clear-bet` is already used by Blackjack (`#bj-clear-bet`), Video Poker (`#vp-clear-bet`), Slots (`#slots-clear-bet`), and Roulette (`#clear-bet-btn`), but it's only defined in `roulette.css`. Every later task in this plan touches a game that depends on this class, so it needs to live in the shared stylesheet first.

**Interfaces:**
- Produces: `.btn-clear-bet` / `.btn-clear-bet:hover` available globally via `style.css` (loaded on every page). No selector or behavior change — pure relocation.

- [ ] **Step 1: Move the CSS rule**

  Remove from `src/main/resources/static/styles/roulette.css` (currently lines 86-100):
  ```css
  .btn-clear-bet {
      background: transparent;
      border: 1px solid var(--steel-dark);
      color: var(--steel);
      border-radius: 4px;
      padding: 2px 8px;
      font-size: 0.75rem;
      cursor: pointer;
      transition: color 0.15s, border-color 0.15s;
  }

  .btn-clear-bet:hover {
      color: #c0392b;
      border-color: #c0392b;
  }
  ```

  Add to `src/main/resources/static/styles/style.css`, right after the `.tutorial-body li` block (currently ends line 667, before `.modal-tab`):
  ```css
  /* ---------- shared "Clear bet" button (all games) ---------- */

  .btn-clear-bet {
    background: transparent;
    border: 1px solid var(--steel-dark);
    color: var(--steel);
    border-radius: 4px;
    padding: 2px 8px;
    font-size: 0.75rem;
    cursor: pointer;
    transition: color 0.15s, border-color 0.15s;
  }

  .btn-clear-bet:hover {
    color: #c0392b;
    border-color: #c0392b;
  }
  ```

- [ ] **Step 2: Verify**

  Run: `./mvnw spring-boot:run`, open Roulette in the browser, place a chip bet, click "✕ Clear". Expected: bet resets to 0, button still looks and behaves exactly as before (transparent, thin border, red on hover).

- [ ] **Step 3: Commit**
  ```bash
  git add src/main/resources/static/styles/roulette.css src/main/resources/static/styles/style.css
  git commit -m "style: move shared clear-bet button into the global stylesheet"
  ```

---

### Task 2: Fix Blackjack cards covering the score

**Files:**
- Modify: `src/main/resources/static/js/blackjack/round.js`

**Root cause:** `renderPlayerCards`, `addPlayerCard`, `renderDealerCards`, `addDealerCard` all move `.bj-card` elements using hand-picked pixel offsets (`{left: -535, top: 440|45}` plus `120 * cardIndex`) measured from the deck's CSS position. These numbers only happen to roughly line up with the `.bj-slot` targets — nothing ties them to the slots' real position, so a card can land on top of `.bj-score`/`.bj-seat-label`, especially mid-animation.

**Interfaces:**
- Produces: `cardTargetOffset(cardEl, handEl, cardIndex)` — internal to `round.js`, returns `{left, top}` in pixels for `move()`.
- Consumes: existing `move(el, aPos, directionChange)` from `js/constants/element.js` (unchanged), existing `#player-hand` / `#dealer-hand` / `.bj-slot` / `#bj-deck` DOM.

- [ ] **Step 1: Add the shared positioning helper and the two hand-element lookups**

  In `src/main/resources/static/js/blackjack/round.js`, after the existing `const playerSlot1 = ...` / `const playerSlot2 = ...` block (around line 54-55), add:
  ```js
  const playerHandEl = document.querySelector("#player-hand");
  const dealerHandEl = document.querySelector("#dealer-hand");

  // Returns the pixel offset (for move()) that lands `cardEl` exactly on the
  // slot at `cardIndex` within `handEl`. Cards beyond the two rendered
  // .bj-slot elements (i.e. hits) fan out to the right of the last slot,
  // spaced the same as the CSS `.bj-hand` gap, so they never land on the
  // score/label above or below the hand.
  function cardTargetOffset(cardEl, handEl, cardIndex) {
    const startRect = cardEl.getBoundingClientRect();
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
  ```

- [ ] **Step 2: Use it in `renderPlayerCards` / `addPlayerCard`**

  Replace (current, lines 96-124):
  ```js
  function renderPlayerCards() {
    playerCardsContainer.innerHTML = "";
    let movement = {
      left: -535,
      top: 440,
    };
    let directionChange = 120;

    gameState.playerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      playerCards++;
      move(cardEl, movement, directionChange * playerCards);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }

  function addPlayerCard() {
    let movement = {
      left: -535,
      top: 440,
    };
    let directionChange = 120;

    const cardEl = element("div", table, ["bj-card"]);
    playerCards++;
    move(cardEl, movement, directionChange * playerCards);
    const lastCard = gameState.playerHand.cards[gameState.playerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
  }
  ```

  With:
  ```js
  function renderPlayerCards() {
    playerCardsContainer.innerHTML = "";

    gameState.playerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      playerCards++;
      move(cardEl, cardTargetOffset(cardEl, playerHandEl, playerCards - 1), 0);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }

  function addPlayerCard() {
    const cardEl = element("div", table, ["bj-card"]);
    playerCards++;
    move(cardEl, cardTargetOffset(cardEl, playerHandEl, playerCards - 1), 0);
    const lastCard = gameState.playerHand.cards[gameState.playerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
  }
  ```

- [ ] **Step 3: Use it in `renderDealerCards` / `addDealerCard`**

  Replace (current, lines 126-153):
  ```js
  function renderDealerCards() {
    playerCardsContainer.innerHTML = "";
    let movement = {
      left: -535,
      top: 45,
    };
    let directionChange = 120;

    gameState.dealerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      dealerCards++;
      move(cardEl, movement, directionChange * dealerCards);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }
  function addDealerCard() {
    let movement = {
      left: -535,
      top: 45,
    };
    let directionChange = 120;

    const cardEl = element("div", table, ["bj-card"]);
    dealerCards++;
    move(cardEl, movement, directionChange * dealerCards);
    const lastCard = gameState.dealerHand.cards[gameState.dealerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
  }
  ```

  With:
  ```js
  function renderDealerCards() {
    playerCardsContainer.innerHTML = "";

    gameState.dealerHand.cards.forEach((c) => {
      const cardEl = element("div", table, ["bj-card"]); // create the card
      dealerCards++;
      move(cardEl, cardTargetOffset(cardEl, dealerHandEl, dealerCards - 1), 0);
      cardEl.style.background = `url(${c.image}) center / cover no-repeat`;
    });
  }
  function addDealerCard() {
    const cardEl = element("div", table, ["bj-card"]);
    dealerCards++;
    move(cardEl, cardTargetOffset(cardEl, dealerHandEl, dealerCards - 1), 0);
    const lastCard = gameState.dealerHand.cards[gameState.dealerHand.cards.length - 1];
    cardEl.style.background = `url(${lastCard.image}) center / cover no-repeat`;
  }
  ```

- [ ] **Step 4: Verify**

  Run: `./mvnw spring-boot:run`, open Blackjack, place a bet, click Start, then Hit two or three times until you either bust or stand. Expected: every dealt and hit card lands exactly in its dashed `.bj-slot` outline, the player and dealer score text stays fully readable throughout (no card ever overlaps `.bj-score`), and this holds after resizing the window (triggers the `@media (max-width: 900px|600px)` slot sizes in `table.css`).

- [ ] **Step 5: Commit**
  ```bash
  git add src/main/resources/static/js/blackjack/round.js
  git commit -m "fix: land blackjack cards on their real slot position so they stop covering the score"
  ```

---

### Task 3: Blackjack — move bet controls into the chip rail

**Files:**
- Modify: `src/main/resources/static/js/blackjack/table-markup.js`
- Modify: `src/main/resources/static/js/blackjack/round.js`
- Modify: `src/main/resources/static/styles/style.css`

**Interfaces:**
- Produces: `.chip-rail-controls` CSS class in `style.css` — a divider + column layout for the bet-amount/Clear/action-button group inside any `#chips.bj-chips` panel. Reused by Tasks 4-6.

- [ ] **Step 1: Add the shared `.chip-rail-controls` style**

  In `src/main/resources/static/styles/style.css`, right after the `.btn-clear-bet:hover` rule added in Task 1, add:
  ```css
  .bj-chips .chip-rail-controls {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    width: 100%;
    margin-top: 4px;
    padding-top: 14px;
    border-top: 1px solid var(--steel-dark);
  }
  ```

- [ ] **Step 2: Move the bet form into `#chips` in `table-markup.js`**

  Replace (current):
  ```html
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
  ```
  and
  ```html
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
  ```

  With:
  ```html
          <div id="chips" class="bj-chips">
            ${CHIPS.map(
              (chip) => `
              <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                <img src="${chip.image}" alt="${chip.value} dollar chip">
              </button>
            `,
            ).join("")}
            <form class="chip-rail-controls">
              <p id="bet-amount" name="amount">
              Bet amount: 0
              </p>
              <button type="button" class="btn-clear-bet" id="bj-clear-bet">✕ Clear</button>
              <button type="submit">
                <span class="button_top">Start</span>
              </button>
            </form>
          </div>

                  <div id="table" class="bj-table">
  ```
  and remove the now-relocated `<p id="ending"></p><form>...</form>` block from where it used to sit inside `#table` (keep `<p id="ending"></p>` where it was, just drop the `<form>` that followed it):
  ```html
          <p id="ending"></p>
  ```

- [ ] **Step 3: Update `round.js` to remove the form from its new parent**

  `table.removeChild(form)` assumed the form was a child of `#table`; it's now a child of `#chips`. Replace both occurrences in `src/main/resources/static/js/blackjack/round.js`:

  Occurrence 1 (inside the `if (gameState.status === "PLAYER_TURN")` branch):
  ```js
  table.removeChild(form);
  ```
  becomes:
  ```js
  form.remove();
  ```

  Occurrence 2 (inside the form's `submit` handler):
  ```js
  table.removeChild(form);
  ```
  becomes:
  ```js
  form.remove();
  ```

- [ ] **Step 4: Verify**

  Run: `./mvnw spring-boot:run`, open Blackjack. Expected: "Bet amount / ✕ Clear / Start" now sit directly under the chip buttons inside the same panel, separated by a thin divider line; picking chips still updates the bet amount; Clear still resets it; Start still deals a hand and removes the form once the round begins; the felt table area under the dealer's hand is now empty (no floating form) until the round ends and "Play again" appears in `#ending`.

- [ ] **Step 5: Commit**
  ```bash
  git add src/main/resources/static/js/blackjack/table-markup.js src/main/resources/static/js/blackjack/round.js src/main/resources/static/styles/style.css
  git commit -m "feat: group blackjack bet controls with the chip rail"
  ```

---

### Task 4: Video Poker — move bet controls into the chip rail

**Files:**
- Modify: `src/main/resources/static/views/pages/videopoker.js`

**Interfaces:**
- Consumes: `.chip-rail-controls` from Task 3.

- [ ] **Step 1: Move the `.bet-panel` markup into `#chips`**

  Replace (current, around lines 48-60):
  ```js
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
  ```
  With:
  ```js
            <div id="chips" class="bj-chips">
  ${CHIPS.map(
    (chip) => `
                <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                  <img src="${chip.image}" alt="${chip.value} dollar chip">
                </button>
              `,
  ).join("")}
              <div class="bet-panel chip-rail-controls"></div>
      </div>
      <div class="vp-table">
            <div id="game">
              <div id="card-slot-container">
      `;
  ```

  Replace (current, around lines 66-81):
  ```js
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
  ```
  With:
  ```js
    cardSlotContainer.insertAdjacentHTML(
      "afterend",
      `
                  <div id="buttonContainer">
                  </div>
                  <p class="vp-result"></p>
                </div>
              </div>
          </div>
      </div>
      `,
    );

    document.querySelector(".bet-panel").insertAdjacentHTML(
      "beforeend",
      `
        <p class="amount">Bet amount: 0</p>
        <button type="button" class="btn-clear-bet" id="vp-clear-bet">✕ Clear</button>
      `,
    );
  ```

  Note: `lowerButContainer` (`document.querySelector(".lower-button-container")`) is still read later (`ensureDrawButton` appends the "Draw" button to it) — since that wrapper `<div class="lower-button-container">` was removed along with the old `.bet-panel` location, replace the `lowerButContainer` lookup with the `#game` element instead, so "Draw" still renders under the cards:

  Replace:
  ```js
    const lowerButContainer = document.querySelector(".lower-button-container");
  ```
  With:
  ```js
    const lowerButContainer = document.querySelector("#game");
  ```

- [ ] **Step 2: Verify**

  Run: `./mvnw spring-boot:run`, open Video Poker. Expected: "Bet amount / ✕ Clear / Bet" sit under the chip buttons in the left rail, separated by the same divider as Blackjack; picking chips, clearing, betting, holding cards, drawing, "Bet Again"/"New Bet" all still work; the "Draw" button still appears under the dealt cards after betting.

- [ ] **Step 3: Commit**
  ```bash
  git add src/main/resources/static/views/pages/videopoker.js
  git commit -m "feat: group video poker bet controls with the chip rail"
  ```

---

### Task 5: Slots — move bet readout and Spin into the chip rail

**Files:**
- Modify: `src/main/resources/static/views/pages/slots.js`
- Modify: `src/main/resources/static/styles/slots.css`

**Interfaces:**
- Consumes: `.chip-rail-controls` from Task 3.

- [ ] **Step 1: Move the bet readout + Spin button markup into `#chips`**

  Locate the template literal in `init()` (around lines 99-127). Replace:
  ```js
          <div class="slots-floor">
              <div id="chips" class="bj-chips">
                  ${CHIPS.map(
                      (chip) => `
                  <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                      <img src="${chip.image}" alt="${chip.value} dollar chip">
                  </button>
                  `,
                  ).join("")}
              </div>
              <div class="slots-machine">
                  <div class="slots-readout">
                      <p>Balance <span id="slots-balance">${player.balance ?? "—"}</span></p>
                      <p>Bet <span id="slots-bet">0</span> <button type="button" class="btn-clear-bet" id="slots-clear-bet">✕ Clear</button></p>
                  </div>
                  <div class="slots-window" id="slots-window">
                      ${reelWindow(0)}
                      ${reelWindow(1)}
                      ${reelWindow(2)}
                  </div>
                  <p class="slots-result" id="slots-result">Pick a bet. Then spin.</p>
                  <button type="button" id="slots-spin">
                      <span class="button_top">Spin</span>
                  </button>
  ```
  With:
  ```js
          <div class="slots-floor">
              <div id="chips" class="bj-chips">
                  ${CHIPS.map(
                      (chip) => `
                  <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                      <img src="${chip.image}" alt="${chip.value} dollar chip">
                  </button>
                  `,
                  ).join("")}
                  <div class="chip-rail-controls">
                      <p>Bet <span id="slots-bet">0</span></p>
                      <button type="button" class="btn-clear-bet" id="slots-clear-bet">✕ Clear</button>
                      <button type="button" id="slots-spin">
                          <span class="button_top">Spin</span>
                      </button>
                  </div>
              </div>
              <div class="slots-machine">
                  <div class="slots-readout">
                      <p>Balance <span id="slots-balance">${player.balance ?? "—"}</span></p>
                  </div>
                  <div class="slots-window" id="slots-window">
                      ${reelWindow(0)}
                      ${reelWindow(1)}
                      ${reelWindow(2)}
                  </div>
                  <p class="slots-result" id="slots-result">Pick a bet. Then spin.</p>
  ```

  Everything after `</button>` in the original (closing `</div></div></section>`) stays as-is — only the button moved up into the new `.chip-rail-controls` block, so the machine's own closing tags no longer need to wrap it. Double-check the final markup balances (`.slots-machine` now closes right after `.slots-result`).

- [ ] **Step 2: Adjust `.slots-readout` for one row instead of two**

  In `src/main/resources/static/styles/slots.css`, `.slots-readout` (currently `display:flex; justify-content:space-between;`) now only holds the Balance line. Change it to left-align since there's nothing to space out against:
  ```css
  .slots-readout {
      margin-bottom: 22px;
      font-size: 0.85rem;
      color: var(--text-dim);
      text-transform: uppercase;
  }
  ```
  (drop `display: flex; justify-content: space-between;`)

- [ ] **Step 3: Verify**

  Run: `./mvnw spring-boot:run`, open Slots. Expected: chip rail now shows chips, then "Bet 0 / ✕ Clear / Spin" below the divider; the machine panel shows only "Balance" above the reels; picking chips, clearing, and spinning all still work, chips/Spin still disable while spinning.

- [ ] **Step 4: Commit**
  ```bash
  git add src/main/resources/static/views/pages/slots.js src/main/resources/static/styles/slots.css
  git commit -m "feat: group slots bet controls with the chip rail"
  ```

---

### Task 6: Roulette — shared chip rail, moved to the side

**Files:**
- Modify: `src/main/resources/static/views/pages/roulette.js`
- Modify: `src/main/resources/static/styles/roulette.css`

**Interfaces:**
- Consumes: `.bj-chips`/`.bj-chip` (from `table.css`, loaded globally via `index.html`), `.chip-rail-controls` and `.btn-clear-bet` (from Task 1/3, `style.css`), `CHIPS` list already used elsewhere (`js/blackjack/chips.js`) — import it instead of hand-rolled chip markup.

- [ ] **Step 1: Confirm `table.css` is loaded on the Roulette page**

  Run: `grep -n "table.css\|roulette.css" src/main/resources/static/index.html`. `table.css` defines `.bj-chips`/`.bj-chip`; if it isn't already linked globally, add it next to `roulette.css`'s `<link>` in `index.html`. (Blackjack/Slots/Peixinho already rely on `.bj-chips` from `table.css`, so it is very likely already global — this step is just a confirmation, not expected to require a change.)

- [ ] **Step 2: Replace the chip-tray markup with the shared rail, importing `CHIPS`**

  Add to the top of `src/main/resources/static/views/pages/roulette.js`:
  ```js
  import { CHIPS } from "/js/blackjack/chips.js";
  ```

  Replace (current, the `.roulette-bet-area` block plus the wrapping structure around it and the table image, lines ~40-96):
  ```html
              <div class="table-view" id="table-view">
                  <div class="roulette-balance">
                      <span>Balance</span>
                      <span id="balance-display">${balance} chips</span>
                  </div>

                  <div class="table-img-wrap">
                      <img src="/assets/roulette/rouletteTable.png" alt="Roulette table" />
                      <button type="button" class="bet-zone zone-red"   data-bettype="RED"   aria-label="Bet Red"></button>
                      <button type="button" class="bet-zone zone-black" data-bettype="BLACK" aria-label="Bet Black"></button>
                      <button type="button" class="bet-zone zone-odd"   data-bettype="ODD"   aria-label="Bet Odd"></button>
                      <button type="button" class="bet-zone zone-even"  data-bettype="EVEN"  aria-label="Bet Even"></button>
                      <button type="button" class="bet-zone zone-low"   data-bettype="LOW"   aria-label="Bet 1 to 18"></button>
                      <button type="button" class="bet-zone zone-high"  data-bettype="HIGH"  aria-label="Bet 19 to 36"></button>
                  </div>

                  <div class="roulette-bet-area">
                      <p class="bet-area-label">Choose your chips</p>
                      <div class="chip-tray">
                          <button class="chip" data-value="1">
                              <img src="/assets/Coins/1dollar.coin.png" alt="1" />
                              <span>1</span>
                          </button>
                          <button class="chip" data-value="5">
                              <img src="/assets/Coins/5dollarcoin.png" alt="5" />
                              <span>5</span>
                          </button>
                          <button class="chip" data-value="10">
                              <img src="/assets/Coins/10dollarcoin.png" alt="10" />
                              <span>10</span>
                          </button>
                          <button class="chip" data-value="25">
                              <img src="/assets/Coins/25dollarcoin.png" alt="25" />
                              <span>25</span>
                          </button>
                          <button class="chip" data-value="50">
                              <img src="/assets/Coins/50dollarcoin.png" alt="50" />
                              <span>50</span>
                          </button>
                          <button class="chip" data-value="100">
                              <img src="/assets/Coins/100dollarcoin.png" alt="100" />
                              <span>100</span>
                          </button>
                      </div>
                      <div class="bet-display">
                          <span class="bet-display-label">Bet:</span>
                          <span id="bet-amount-display">0</span>
                          <span class="bet-display-label">chips</span>
                          <button class="btn-clear-bet" id="clear-bet-btn">✕ Clear</button>
                      </div>
                  </div>

                  <p class="roulette-bet-selected" id="bet-selected">No bet selected</p>
                  <p class="modal-error" id="roulette-error"></p>

                  <button class="btn roulette-spin-btn" id="spin-btn">SPIN</button>
              </div>
  ```

  With:
  ```html
              <div class="table-view" id="table-view">
                  <div class="roulette-balance">
                      <span>Balance</span>
                      <span id="balance-display">${balance} chips</span>
                  </div>

                  <div class="bj-floor">
                      <div id="chips" class="bj-chips">
                          ${CHIPS.map(
                              (chip) => `
                          <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                              <img src="${chip.image}" alt="${chip.value} dollar chip">
                          </button>
                          `,
                          ).join("")}
                          <div class="chip-rail-controls">
                              <p class="bet-display">
                                  <span class="bet-display-label">Bet:</span>
                                  <span id="bet-amount-display">0</span>
                                  <span class="bet-display-label">chips</span>
                              </p>
                              <button class="btn-clear-bet" id="clear-bet-btn">✕ Clear</button>
                              <p class="roulette-bet-selected" id="bet-selected">No bet selected</p>
                              <button class="btn roulette-spin-btn" id="spin-btn">SPIN</button>
                          </div>
                      </div>

                      <div class="table-img-wrap">
                          <img src="/assets/roulette/rouletteTable.png" alt="Roulette table" />
                          <button type="button" class="bet-zone zone-red"   data-bettype="RED"   aria-label="Bet Red"></button>
                          <button type="button" class="bet-zone zone-black" data-bettype="BLACK" aria-label="Bet Black"></button>
                          <button type="button" class="bet-zone zone-odd"   data-bettype="ODD"   aria-label="Bet Odd"></button>
                          <button type="button" class="bet-zone zone-even"  data-bettype="EVEN"  aria-label="Bet Even"></button>
                          <button type="button" class="bet-zone zone-low"   data-bettype="LOW"   aria-label="Bet 1 to 18"></button>
                          <button type="button" class="bet-zone zone-high"  data-bettype="HIGH"  aria-label="Bet 19 to 36"></button>
                      </div>
                  </div>

                  <p class="modal-error" id="roulette-error"></p>
              </div>
  ```

  Note `data-value` (old) becomes `data-chip` (matches `CHIPS`/`.bj-chip` convention). Update the chip click handler accordingly (Step 3).

- [ ] **Step 3: Update `setupGame()` for the new chip markup**

  Replace (current, `document.querySelectorAll(".chip").forEach(...)`):
  ```js
    document.querySelectorAll(".chip").forEach(chip => {
        chip.addEventListener("click", () => {
            const value = parseInt(chip.dataset.value, 10);
  ```
  With:
  ```js
    document.querySelectorAll(".bj-chip").forEach(chip => {
        chip.addEventListener("click", () => {
            const value = parseInt(chip.dataset.chip, 10);
  ```
  (rest of that handler body is unchanged)

- [ ] **Step 4: Drop the old chip-tray CSS, restyle the rail area**

  In `src/main/resources/static/styles/roulette.css`, remove the now-unused `.chip-tray`, `.chip`, `.chip:hover`, `.chip:active`, `.chip img`, `.chip span`, and `.roulette-bet-area`/`.bet-area-label` rules (superseded by the shared `.bj-chips`/`.bj-chip`/`.chip-rail-controls`). Also remove `.bet-display`'s `justify-content: center` (it now sits in a narrower column rail, left-aligned like the rest of `.chip-rail-controls`) — keep `.bet-display { display:flex; align-items:center; gap:8px; font-size:1rem; font-weight:700; }`.

  Add, so the table image and rail sit side by side and the rail matches the other games' width. Scope it to `.roulette-page` rather than overriding the bare `.bj-floor`/`.table-img-wrap` classes — `roulette.css` loads after `table.css`, so an unscoped override would silently win there today but break the moment load order changes, and it would leak into any other page that ever reuses `.bj-floor`:
  ```css
  .roulette-page .bj-floor {
      width: 100%;
      max-width: 900px;
      align-items: flex-start;
  }

  .roulette-page .table-img-wrap {
      max-width: 700px;
  }
  ```

- [ ] **Step 5: Verify**

  Run: `./mvnw spring-boot:run`, open Roulette. Expected: chip rail (same look as Blackjack/Slots/Video Poker) sits to the left of the table image; picking a bet zone, picking chips, clearing, and spinning all still work end to end (bet placed, wheel spins, result shown, balance updates); responsive layout at narrower widths still usable (check the existing `@media (max-width: 900px|760px)` blocks still make sense with the new `.bj-floor`/`.bj-chips` responsive rules from `table.css`).

- [ ] **Step 6: Commit**
  ```bash
  git add src/main/resources/static/views/pages/roulette.js src/main/resources/static/styles/roulette.css
  git commit -m "feat: uniformize roulette's chip rail with the other games"
  ```

---

### Task 7: Full-app QA pass, code review, PR

**Files:** none (verification + review only)

- [ ] **Step 1: Manual pass through every affected page**

  With `./mvnw spring-boot:run` running, walk through: Home → create/reuse a player → Lobby → each of Blackjack, Roulette, Peixinho (unchanged, confirm still fine), Slots, Video Poker → Profile. For each of Blackjack/Video Poker/Slots/Roulette confirm the bet rail (chips + amount + Clear + action button) is visually one grouped panel, and for Blackjack confirm the score-covering bug from the original report no longer reproduces after several hits.

- [ ] **Step 2: Code review**

  Invoke the `code-review` skill against the full branch diff (`main`/`dev`...HEAD). Address any correctness findings; ponytail-appropriate simplifications only if they don't reopen the bugs just fixed.

- [ ] **Step 3: Open the PR**

  Push the branch and open a PR into `dev` (English title/body, no AI/tool mentions), summarizing the two bug fixes and the chip-rail uniformization, with the manual QA pass above as the test plan.
