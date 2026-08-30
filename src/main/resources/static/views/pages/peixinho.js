import router from "../../router.js";
import { API_BASE_URL } from "../../js/constants/utils.js";

const DEFAULT_BET = 100;
const VALUE_LABELS = { ACE: "Ás", JACK: "Valete", QUEEN: "Dama", KING: "Rei" };

async function api(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    });
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        const error = new Error(body?.message || `Request failed (${response.status})`);
        error.status = response.status;
        throw error;
    }
    return body;
}

function hiddenCardMarkup() {
    return `<li class="px-card px-card-hidden" aria-label="Carta do adversário virada para baixo"></li>`;
}

function visibleCardMarkup(card) {
    return `<li class="px-card px-card-visible" aria-label="Carta ${card.code}">${card.code}</li>`;
}

function opponentId(state, playerId) {
    return Object.keys(state.opponentHandSizes).find((id) => id !== playerId);
}

function bookCount(state, playerId) {
    return state.books.filter((book) => book.playerId === playerId).length;
}

function requestOptions(hand) {
    const values = [...new Set(hand.map((card) => card.value))];
    return values.map((value) =>
        `<option value="${value}">${VALUE_LABELS[value] || value}</option>`,
    ).join("");
}

function feedbackClass(result) {
    if (result.gotCards) return "px-feedback-received";
    if (result.drewFromDeck && /play again/i.test(result.message)) return "px-feedback-again";
    return "px-feedback-pass";
}

function feedbackLabel(result) {
    if (result.gotCards) return "Recebeste cartas — jogas outra vez";
    if (result.drewFromDeck && /play again/i.test(result.message)) return "Foi à pesca — jogas outra vez";
    return "Foi à pesca — passaste a vez";
}

function renderGame(main, state, feedback = null) {
    const playerId = sessionStorage.getItem("playerId");
    const otherPlayerId = opponentId(state, playerId);
    const opponentCount = otherPlayerId ? state.opponentHandSizes[otherPlayerId] : 0;
    const canAsk = state.currentPlayerId === playerId && Boolean(otherPlayerId);
    const playerBooks = bookCount(state, playerId);
    const opponentBooks = state.books.length - playerBooks;

    main.innerHTML = `
        <section class="px-page" aria-labelledby="px-title">
            <div class="px-heading">
                <p class="px-kicker">Bending Odds Casino</p>
                <h1 id="px-title">Peixinho</h1>
                <p class="px-subtitle">Pede uma carta e completa os teus conjuntos.</p>
            </div>

            ${feedback ? `<section class="px-feedback ${feedbackClass(feedback)}" role="status">
                <strong>${feedbackLabel(feedback)}</strong>
                <span>${feedback.message}</span>
            </section>` : ""}

            <div class="px-table">
                <section class="px-zone px-opponent-zone" aria-labelledby="px-opponent-title">
                    <div class="px-zone-heading">
                        <div><p class="px-label">Adversário</p><h2 id="px-opponent-title">Dealer</h2></div>
                        <span class="px-count">${opponentCount} cartas</span>
                    </div>
                    <ul class="px-hand px-hidden-hand" aria-label="Mão do adversário, cartas escondidas">
                        ${Array.from({ length: opponentCount }, hiddenCardMarkup).join("")}
                    </ul>
                </section>

                <div class="px-middle-row">
                    <section class="px-sets px-sets-opponent" aria-label="Conjuntos do adversário">
                        <p class="px-label">Conjuntos do dealer</p>
                        <strong class="px-set-count">${opponentBooks}</strong>
                        <span>fechados</span>
                    </section>
                    <section class="px-deck-zone" aria-label="Monte">
                        <div class="px-deck" aria-hidden="true">
                            <span class="px-deck-card"></span><span class="px-deck-card"></span><span class="px-deck-card"></span>
                        </div>
                        <p class="px-label">Monte</p>
                        <strong class="px-deck-count">${state.deckSize}</strong>
                        <span>cartas restantes</span>
                    </section>
                    <section class="px-sets px-sets-player" aria-label="Conjuntos do jogador">
                        <p class="px-label">Os teus conjuntos</p>
                        <strong class="px-set-count">${playerBooks}</strong>
                        <span>fechados</span>
                    </section>
                </div>

                <section class="px-zone px-player-zone" aria-labelledby="px-player-title">
                    <div class="px-zone-heading">
                        <div><p class="px-label">Jogador</p><h2 id="px-player-title">A tua mão</h2></div>
                        <span class="px-count">${state.playerHand.length} cartas</span>
                    </div>
                    <ul class="px-hand px-visible-hand" aria-label="A tua mão">
                        ${state.playerHand.map(visibleCardMarkup).join("")}
                    </ul>
                </section>
            </div>

            <section class="px-request" aria-labelledby="px-request-title">
                <div>
                    <p class="px-label">${canAsk ? "A tua vez" : "Vez do adversário"}</p>
                    <h2 id="px-request-title">Pedir carta</h2>
                </div>
                <div class="px-request-controls">
                    <label for="px-card-value">Valor</label>
                    <select id="px-card-value" ${canAsk ? "" : "disabled"}>
                        ${requestOptions(state.playerHand)}
                    </select>
                    <button class="px-button" id="px-ask" type="button" ${canAsk ? "" : "disabled"}>Pedir carta</button>
                </div>
                <p class="px-coming-soon">Só podes pedir valores que já tens na mão.</p>
            </section>
        </section>
    `;

    if (canAsk) {
        document.querySelector("#px-ask").addEventListener("click", async () => {
            const button = document.querySelector("#px-ask");
            button.disabled = true;
            try {
                const result = await api("peixinho/ask", {
                    method: "POST",
                    body: JSON.stringify({
                        playerId,
                        targetId: otherPlayerId,
                        cardValue: document.querySelector("#px-card-value").value,
                    }),
                });
                renderGame(main, result.gameState, result);
            } catch (error) {
                renderError(main, state, error);
            }
        });
    }
}

function renderError(main, state, error) {
    renderGame(main, state);
    const errorBox = document.createElement("p");
    errorBox.className = "px-error";
    errorBox.setAttribute("role", "alert");
    errorBox.textContent = error.message;
    document.querySelector(".px-request").prepend(errorBox);
}

async function loadGame(main, playerId) {
    try {
        return await api(`peixinho/state/${playerId}`);
    } catch (error) {
        if (error.status !== 404) throw error;
        return api("peixinho/start", {
            method: "POST",
            body: JSON.stringify({ playerId, bet: DEFAULT_BET }),
        });
    }
}

export function init() {
    const main = document.querySelector("main");
    const playerId = sessionStorage.getItem("playerId");

    if (!playerId) {
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

    main.innerHTML = `<p class="px-loading" role="status">A preparar a mesa…</p>`;
    loadGame(main, playerId)
        .then((state) => renderGame(main, state))
        .catch((error) => {
            main.innerHTML = `<p class="px-error" role="alert">Não foi possível carregar a mesa: ${error.message}</p>`;
        });
}
