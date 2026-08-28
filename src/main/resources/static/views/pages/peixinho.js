import router from "../../router.js";

const mockPlayerHand = ["AS", "10H", "10C", "7D", "KC"];
const mockOpponentCardCount = 7;
const mockDeckCount = 32;

function cardMarkup(card) {
    return `<li class="px-card px-card-visible" aria-label="Carta ${card}">${card}</li>`;
}

function hiddenCardMarkup() {
    return `<li class="px-card px-card-hidden" aria-label="Carta do adversário virada para baixo"></li>`;
}

export function init() {
    const main = document.querySelector("main");

    if (!sessionStorage.getItem("playerId")) {
        main.innerHTML = `
            <section class="join">
                <h2>No player yet</h2>
                <p>Create a player before sitting at the table.</p>
                <a id="go-home" href="/" class="btn">Go back</a>
            </section>
        `;
        document.querySelector("#go-home").addEventListener("click", (event) => {
            event.preventDefault();
            router.navigate("/");
        });
        return;
    }

    // TODO: fetch a /api/peixinho/state when X-B1 is ready.
    main.innerHTML = `
        <section class="px-page" aria-labelledby="px-title">
            <div class="px-heading">
                <p class="px-kicker">Bending Odds Casino</p>
                <h1 id="px-title">Peixinho</h1>
                <p class="px-subtitle">Pede uma carta e completa os teus conjuntos.</p>
            </div>

            <div class="px-table">
                <section class="px-zone px-opponent-zone" aria-labelledby="px-opponent-title">
                    <div class="px-zone-heading">
                        <div>
                            <p class="px-label">Adversário</p>
                            <h2 id="px-opponent-title">Dealer</h2>
                        </div>
                        <span class="px-count" aria-label="${mockOpponentCardCount} cartas na mão">
                            ${mockOpponentCardCount} cartas
                        </span>
                    </div>
                    <ul class="px-hand px-hidden-hand" aria-label="Mão do adversário, cartas escondidas">
                        ${Array.from({ length: mockOpponentCardCount }, hiddenCardMarkup).join("")}
                    </ul>
                </section>

                <div class="px-middle-row">
                    <section class="px-sets px-sets-opponent" aria-label="Conjuntos do adversário">
                        <p class="px-label">Conjuntos do dealer</p>
                        <strong class="px-set-count">0</strong>
                        <span>fechados</span>
                    </section>

                    <section class="px-deck-zone" aria-label="Monte">
                        <div class="px-deck" aria-hidden="true">
                            <span class="px-deck-card"></span>
                            <span class="px-deck-card"></span>
                            <span class="px-deck-card"></span>
                        </div>
                        <p class="px-label">Monte</p>
                        <strong class="px-deck-count">${mockDeckCount}</strong>
                        <span>cartas restantes</span>
                    </section>

                    <section class="px-sets px-sets-player" aria-label="Conjuntos do jogador">
                        <p class="px-label">Os teus conjuntos</p>
                        <strong class="px-set-count">1</strong>
                        <span>fechado</span>
                    </section>
                </div>

                <section class="px-zone px-player-zone" aria-labelledby="px-player-title">
                    <div class="px-zone-heading">
                        <div>
                            <p class="px-label">Jogador</p>
                            <h2 id="px-player-title">A tua mão</h2>
                        </div>
                        <span class="px-count">${mockPlayerHand.length} cartas</span>
                    </div>
                    <ul class="px-hand px-visible-hand" aria-label="A tua mão">
                        ${mockPlayerHand.map(cardMarkup).join("")}
                    </ul>
                </section>
            </div>

            <section class="px-request" aria-labelledby="px-request-title">
                <div>
                    <p class="px-label">A tua vez</p>
                    <h2 id="px-request-title">Pedir carta</h2>
                </div>
                <div class="px-request-controls">
                    <label for="px-card-value">Valor</label>
                    <select id="px-card-value" disabled>
                        <option>2</option>
                        <option>3</option>
                        <option>4</option>
                        <option>5</option>
                        <option>6</option>
                        <option>7</option>
                        <option>8</option>
                        <option>9</option>
                        <option>10</option>
                        <option>J</option>
                        <option>Q</option>
                        <option>K</option>
                        <option>Ás</option>
                    </select>
                    <button class="px-button" type="button" disabled>Pedir carta</button>
                </div>
                <p class="px-coming-soon">A mesa estará disponível quando o jogo estiver ligado.</p>
            </section>
        </section>
    `;
}
