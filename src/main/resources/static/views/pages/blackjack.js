import { element, button, stylizedButton } from "../../js/constants/element.js";
import {
  getState,
  startGame,
  playerHit,
  playerStand,
} from "../../js/services/blackjack-service.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { tableMarkup } from "../../js/blackjack/table-markup.js";
import router from "../../router.js";
import { startRound } from "../../js/blackjack/round.js";

export function init() {
  const main = document.querySelector("main");

  // No player session -> the API can't work (playerId would be null).
  // Send them to the home screen to create a player first.
  if (!sessionStorage.getItem("playerId")) {
    main.innerHTML = `
      <section class="join">
        <h2>No player yet</h2>
        <p>Create a player before sitting at the table.</p>
        <a id="go-home" href="/" class="btn">Go back</a>
      </section>
    `;
    document.querySelector("#go-home").addEventListener("click", (e) => {
      e.preventDefault();
      router.navigate("/");
    });
    return;
  }

  // No player session -> the API can't work (playerId would be null).
  // Send them to the home screen to create a player first.
  if (!sessionStorage.getItem("playerId")) {
    main.innerHTML = `
      <section class="join">
        <h2>No player yet</h2>
        <p>Create a player before sitting at the table.</p>
        <a id="go-home" href="/" class="btn">Go back</a>
      </section>
    `;
    document.querySelector("#go-home").addEventListener("click", (e) => {
      e.preventDefault();
      router.navigate("/");
    });
    return;
  }

  const start = stylizedButton(main, "Start");

  start.addEventListener("click", async () => {
    main.removeChild(start);
    await startRound();
  });
}
