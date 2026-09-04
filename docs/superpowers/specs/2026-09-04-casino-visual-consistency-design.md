# Casino visual consistency: unified bet rail + Blackjack fixes

## Context

The frontend already shares a coherent token system (`--gold`/`--felt`/`--panel`
CSS variables, IBM Plex Mono, shared felt-table look for Blackjack/Peixinho/Video
Poker). The gaps are structural, not palette-level:

1. Blackjack: flying cards can visually cover the player/dealer score during
   the deal animation.
2. Blackjack and Video Poker and Slots: the bet-amount readout, "Clear" button,
   and primary action button (Start/Bet/Spin) are not visually grouped with the
   chip rail — they float elsewhere in the felt/machine area.
3. Roulette uses its own chip component (`.chip-tray`/`.chip`, 52px images,
   value label under each coin) instead of the shared `.bj-chips`/`.bj-chip`
   rail, and lays its chips out horizontally under the table image instead of
   in a side rail.

Peixinho already groups chips + bet total + "Sentar à mesa" in one screen and
needs no change. Home/Lobby/Profile already use the shared token system and
need no change.

## Goals

- One visual "bet rail" pattern: chip buttons, then bet-amount readout, Clear,
  and the primary action button, stacked in the same `#chips.bj-chips` panel.
  Applies to Blackjack, Video Poker, Slots, Roulette.
- Roulette's chip rail moves to the side (reusing `.bj-floor` as the row
  wrapper, like Blackjack/Slots already do) and its chips match the other
  games' size/look (drop the redundant text value label; the coin art already
  shows the value, same `/assets/Coins/` images used elsewhere).
- Blackjack cards land exactly on their `.bj-slot` target instead of using
  hardcoded pixel offsets, so they can no longer render on top of the score.

## Non-goals

- No new color palette, font, or global layout system — the existing tokens
  already deliver the "Bender robotic casino" identity consistently.
- No changes to Peixinho, Home, Lobby, or Profile markup/CSS.
- No backend changes.

## Design

### 1. Blackjack card positioning (root-cause fix)

`js/blackjack/round.js` currently animates `.bj-card` elements with
hand-picked pixel offsets (`move(cardEl, {left:-535, top:440|45}, 120*n)`)
relative to the deck's CSS position. This is fragile and can overlap the score
element.

Replace with a single function, used by both the player and dealer render/add
paths, that reads the real target `.bj-slot` element's position (via
`getBoundingClientRect()` against `#table`) for the given card index and
positions the card there. One function, all four call sites
(`renderPlayerCards`, `addPlayerCard`, `renderDealerCards`, `addDealerCard`)
route through it — fixing the root cause once instead of adjusting magic
numbers per call site. Extra cards beyond the two dealt slots (hits) fan out
from the last slot's position using the existing `.bj-hand` gap.

### 2. Bet rail markup (Blackjack, Video Poker, Slots)

`#chips.bj-chips` gains the bet-amount readout, Clear button, and the
game's primary action button (Start / Bet / Spin) as children, appended after
the chip buttons. No new CSS component is needed — `.bj-chips` is already
`display:flex; flex-direction:column; gap:14px`, so appending these controls
stacks them under the chips automatically. Existing element/class
(`.btn-clear-bet`, `stylizedButton`) and existing state/event-handling logic
are reused unchanged — only their DOM location moves.

Each game's `<form>`/control block that currently lives inside the felt/machine
area is removed from there and re-emitted inside `#chips` instead.

### 3. Roulette: shared chip rail, moved to the side

- Replace `.chip-tray`/`.chip` markup with the shared `#chips.bj-chips`/
  `.bj-chip` markup (same `CHIPS` list already imported elsewhere), including
  the bet-amount readout, Clear, and the "SPIN" button per §2.
- Wrap the table image and the new `#chips` rail in a `.bj-floor` row (reused
  from Blackjack/Slots) instead of the current top-to-bottom stack.
- Drop the per-chip value `<span>` — the coin art already shows the value,
  matching how Blackjack/Slots/Video Poker chips work.
- `roulette.css` loses `.chip-tray`/`.chip` (superseded by the shared rail);
  `.roulette-bet-area` shrinks to whatever wrapper is still needed for the
  "Choose your chips" label, if kept.

### 4. Testing

No frontend test suite exists in this repo (vanilla JS, no build/test
tooling); backend tests are out of scope since nothing backend changes.
Verification is a manual pass in the browser (via Playwright) through all five
games plus Lobby/Profile, confirming: bet rail groups chips + amount + Clear +
action button consistently, Roulette's rail sits beside the table, and the
Blackjack score stays visible through a full deal + hit animation.

## Files touched

- `src/main/resources/static/js/blackjack/round.js` (card positioning fix,
  bet-rail markup wiring)
- `src/main/resources/static/js/blackjack/table-markup.js` (move bet
  controls into `#chips`)
- `src/main/resources/static/styles/table.css` (adjust `.bj-table` layout
  now that the felt no longer hosts the bet form; add slot-relative card
  positioning support if needed)
- `src/main/resources/static/views/pages/videopoker.js` (move bet-panel into
  `#chips`)
- `src/main/resources/static/views/pages/slots.js` (move bet readout/Spin
  into `#chips`)
- `src/main/resources/static/views/pages/roulette.js` (switch to shared chip
  rail markup, side-by-side layout)
- `src/main/resources/static/styles/roulette.css` (drop `.chip-tray`/`.chip`,
  adjust `.roulette-page`/`.roulette-bet-area` for the side rail)
- `src/main/resources/static/styles/style.css` (only if the shared `.bj-chips`
  panel needs a small addition, e.g. a divider before the bet controls)
