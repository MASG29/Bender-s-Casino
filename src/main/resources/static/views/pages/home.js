import { element } from "../../js/constants/element.js";
import { createPlayer } from "../../js/services/player-service.js";

export function init() {
  const main = document.querySelector("main");
  main.innerHTML = `
    <section class="join" id="join">
      <h2>Membership Has Its Dents</h2>
      <p>Sign up for a starting stack and a dealer who remembers every bet you've lost.</p>
      <a id="create-name" href="#" class="btn">Pick A Name</a>
    </section>

    <section class="hero">
      <div class="hero-image">
        <img class="bender-img" src="/assets/img_1.png" alt="Robot casino dealer mascot">
      </div>
      <h1>The House Always Bends The Rules</h1>
      <div class="hero-text">
        <p>A casino floor run by a chrome-plated dealer with no interest in your winning streak.</p>
      </div>
    </section>
    `;

  const button = document.querySelector("#create-name");
  button.addEventListener("click", (e) => {});
  const createBtn = document.querySelector("#create-name");
  createBtn.addEventListener("click", () => {
    const form = document.createElement("form");

    const input = document.createElement("input");
    input.type = "text";
    input.name = "name";
    input.placeholder = "O teu nome";
    input.required = "true";
    const submit = document.createElement("button");
    submit.type = "submit";
    submit.textContent = "Submit";

    form.append(input, submit);
    createBtn.replaceWith(form); // ou form.appendChild depois depende onde quiseres pôr

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const player = await createPlayer(input.value);
      savePlayerId(player.playerId);
      router.navigate("/lobby");
    });
  });
}
