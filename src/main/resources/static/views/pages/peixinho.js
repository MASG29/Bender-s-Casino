import router from "../../router.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { API_BASE_URL } from "../../js/constants/utils.js";

const VALUE_LABELS = { ACE: "Ás", JACK: "Valete", QUEEN: "Dama", KING: "Rei" };
const CARD_BACK_IMAGE = "/assets/Cards.Back/red.card.png";
const BET_STORAGE_KEY = "peixinhoBet";
let feedbackTimer;
let botFeedbackTimer;
let currentBet = Number(sessionStorage.getItem(BET_STORAGE_KEY)) || 0;

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

function visibleCardMarkup(card) {
    return `<li class="px-card px-card-visible" aria-label="Carta ${card.code}"
        style="background: url('${card.image}') center / cover no-repeat"></li>`;
}

function hiddenCardMarkup() {
    return `<li class="px-card px-card-hidden" aria-label="Carta do adversário virada para baixo"
        style="background: url('${CARD_BACK_IMAGE}') center / cover no-repeat"></li>`;
}

function opponentEntries(state, playerId) {
    return Object.entries(state.opponentHandSizes)
        .filter(([id]) => id !== playerId);
}

function bookCount(state, playerId) {
    return state.books.filter((book) => book.playerId === playerId).length;
}

function rememberBet(bet) {
    currentBet = bet;
    sessionStorage.setItem(BET_STORAGE_KEY, String(bet));
}

function requestOptions(hand) {
    const values = [...new Set(hand.map((card) => card.value))];
    return values.map((value) =>
        `<option value="${value}">${VALUE_LABELS[value] || value}</option>`,
    ).join("");
}

function opponentMarkup(state, opponents) {
    return opponents.map(([id, handSize], index) => `
        <section class="px-zone px-opponent-zone" aria-label="Bot ${index + 1}">
            <div class="px-zone-heading">
                <div class="px-seat-identity">
                    <span class="px-bot-avatar" aria-hidden="true">B${index + 1}</span>
                    <div><p class="px-label">Adversário</p><h2>Bot ${index + 1}</h2></div>
                </div>
                <span class="px-count">${handSize} cartas</span>
            </div>
            <ul class="px-hand px-hidden-hand" aria-label="Mão do bot ${index + 1}, cartas escondidas">
                ${handSize > 0 ? hiddenCardMarkup() : ""}
            </ul>
            <p class="px-opponent-books">${bookCount(state, id)} conjuntos fechados</p>
        </section>
    `).join("");
}

function feedbackKind(result) {
    if (result.gotCards) return { className: "px-feedback-received", title: "Recebeste cartas", detail: "Jogas outra vez." };
    if (result.drewFromDeck && /play again/i.test(result.message)) {
        return { className: "px-feedback-again", title: "Foste à pesca", detail: "Calhou o valor pedido — jogas outra vez." };
    }
    return { className: "px-feedback-pass", title: "Foste à pesca", detail: "Não calhou — passaste a vez." };
}

function showFeedbackModal(main, className, title, details, message) {
    clearTimeout(feedbackTimer);
    document.querySelector(".px-feedback-backdrop")?.remove();

    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop px-feedback-backdrop open";
    backdrop.innerHTML = `
        <section class="modal-box px-feedback-modal ${className}" role="dialog" aria-modal="true" aria-labelledby="px-feedback-title">
            <button class="modal-close" type="button" aria-label="Fechar resultado">×</button>
            <p class="px-label">Resultado da jogada</p>
            <h2 id="px-feedback-title" class="modal-title">${title}</h2>
            <dl class="px-feedback-details">
                ${details}
            </dl>
            ${message ? `<p class="modal-hint">${message}</p>` : ""}
        </section>
    `;

    const modal = backdrop.querySelector(".px-feedback-modal");
    const closeOnOutside = (event) => {
        if (!modal.contains(event.target)) close();
    };
    const close = () => {
        clearTimeout(feedbackTimer);
        document.removeEventListener("click", closeOnOutside);
        backdrop.remove();
    };
    backdrop.querySelector(".modal-close").addEventListener("click", close);
    main.append(backdrop);
    setTimeout(() => document.addEventListener("click", closeOnOutside, { once: true }), 0);
    feedbackTimer = setTimeout(close, 4500);
}

function showFeedback(main, result, cardValue) {
    clearTimeout(botFeedbackTimer);
    const kind = feedbackKind(result);
    showFeedbackModal(main, kind.className, kind.title, `
        <div><dt>Jogador</dt><dd>Tu</dd></div>
        <div><dt>Pedido</dt><dd>${VALUE_LABELS[cardValue] || cardValue}</dd></div>
        <div><dt>Resultado</dt><dd>${kind.detail}</dd></div>
        <div><dt>Conjunto</dt><dd>${result.formedBook ? "Formaste um conjunto." : "Nenhum conjunto formado."}</dd></div>
    `, result.message);
}

function showBotFeedback(main, botAsk) {
    const details = botAsk.fished
        ? `
            <div><dt>Jogador</dt><dd>Bot</dd></div>
            <div><dt>Resultado</dt><dd>O bot foi pescar ao monte.</dd></div>
            <div><dt>Conjunto</dt><dd>${botAsk.formedBook ? "Formou um conjunto." : "Nenhum conjunto formado."}</dd></div>
        `
        : `
            <div><dt>Jogador</dt><dd>Bot</dd></div>
            <div><dt>Pedido</dt><dd>${VALUE_LABELS[botAsk.cardValue] || botAsk.cardValue}</dd></div>
            <div><dt>Resultado</dt><dd>${botAsk.gotCards ? "Recebeu cartas." : "Não recebeu cartas."}</dd></div>
            <div><dt>Conjunto</dt><dd>${botAsk.formedBook ? "Formou um conjunto." : "Nenhum conjunto formado."}</dd></div>
        `;
    showFeedbackModal(main, "px-feedback-bot", "Jogada do bot", details);
}

function scheduleBotFeedback(main, botAsk, delay = 4700) {
    clearTimeout(botFeedbackTimer);
    botFeedbackTimer = setTimeout(() => showBotFeedback(main, botAsk), delay);
}

function renderGameResult(main, state, playerId, opponents) {
    const playerBooks = bookCount(state, playerId);
    const opponentBooks = state.books.length - playerBooks;
    const playerWon = playerBooks > opponentBooks;
    const payout = currentBet * playerBooks;
    const betLabel = currentBet > 0 ? currentBet : "a mesma aposta";

    clearTimeout(feedbackTimer);
    clearTimeout(botFeedbackTimer);
    document.querySelector(".px-feedback-backdrop")?.remove();
    main.innerHTML = `
        <section class="px-result-screen" aria-labelledby="px-result-title">
            <section class="modal-box px-game-result ${playerWon ? "px-game-result-win" : "px-game-result-loss"}">
                <p class="px-label">Partida terminada</p>
                <h1 id="px-result-title" class="modal-title">${playerWon ? "Ganhaste!" : "Perdeste"}</h1>
                <p class="px-result-summary">${playerWon
        ? `Ganharam-se <strong>${payout}</strong> créditos.`
        : `Apostaste <strong>${currentBet}</strong> créditos.`}</p>
                <div class="px-final-books" aria-label="Contagem final de conjuntos">
                    <div><span>Tu</span><strong>${playerBooks}</strong><small>conjuntos</small></div>
                    <div><span>${opponents.length === 1 ? "Bot" : "Adversários"}</span><strong>${opponentBooks}</strong><small>conjuntos</small></div>
                </div>
                <div class="px-result-actions">
                    <button id="px-play-again" class="px-button" type="button" ${currentBet > 0 ? "" : "disabled"}>Apostar ${betLabel} outra vez</button>
                    <button id="px-new-bet" class="px-button px-button-secondary" type="button">Nova aposta</button>
                </div>
                <p id="px-result-error" class="px-error" role="alert"></p>
            </section>
        </section>
    `;

    document.querySelector("#px-new-bet").addEventListener("click", () => renderBetScreen(main, playerId));
    document.querySelector("#px-play-again").addEventListener("click", async () => {
        const playAgain = document.querySelector("#px-play-again");
        playAgain.disabled = true;
        try {
            const nextState = await api("peixinho/start", {
                method: "POST",
                body: JSON.stringify({ playerId, bet: currentBet }),
            });
            renderGame(main, nextState);
        } catch (error) {
            document.querySelector("#px-result-error").textContent = error.message;
            playAgain.disabled = false;
        }
    });
}

function renderGame(main, state) {
    const playerId = sessionStorage.getItem("playerId");
    const opponents = opponentEntries(state, playerId);
    if (state.status === "FINISHED") {
        renderGameResult(main, state, playerId, opponents);
        return;
    }
    const canAsk = state.status === "PLAYING"
        && state.currentPlayerId === playerId
        && opponents.length > 0
        && state.playerHand.length > 0;
    const playerBooks = bookCount(state, playerId);

    main.innerHTML = `
        <section class="px-page" aria-labelledby="px-title">
            <div class="px-heading">
                <p class="px-kicker">Bending Odds Casino</p>
                <h1 id="px-title">Peixinho</h1>
                <p class="px-subtitle">Pede uma carta e completa os teus conjuntos.</p>
            </div>

            <div class="px-table">
                <div class="px-opponents" aria-label="Adversários à mesa">
                    ${opponentMarkup(state, opponents)}
                </div>

                <div class="px-middle-row">
                    <section class="px-sets px-sets-player" aria-label="Conjuntos do jogador">
                        <p class="px-label">Os teus conjuntos</p>
                        <strong class="px-set-count">${playerBooks}</strong>
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
                    <section class="px-sets px-sets-opponent" aria-label="Conjuntos dos adversários">
                        <p class="px-label">Conjuntos adversários</p>
                        <strong class="px-set-count">${state.books.length - playerBooks}</strong>
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
                    <select id="px-card-value" ${canAsk ? "" : "disabled"}>${requestOptions(state.playerHand)}</select>
                    <button class="px-button" id="px-ask" type="button" ${canAsk ? "" : "disabled"}>Pedir carta</button>
                </div>
                <p class="px-coming-soon">Só podes pedir valores que já tens na mão.</p>
            </section>
        </section>
    `;

    if (canAsk) {
        document.querySelector("#px-ask").addEventListener("click", async () => {
            const button = document.querySelector("#px-ask");
            const cardValue = document.querySelector("#px-card-value").value;
            button.disabled = true;
            try {
                const result = await api("peixinho/ask", {
                    method: "POST",
                    body: JSON.stringify({ playerId, targetId: opponents[0][0], cardValue }),
                });
                renderGame(main, result.gameState);
                if (result.gameState.status !== "FINISHED") showFeedback(main, result, cardValue);
                if (result.botAsk) {
                    scheduleBotFeedback(main, result.botAsk, result.gameState.status === "FINISHED" ? 0 : 4700);
                }
            } catch (error) {
                renderError(main, state, error);
            }
        });
    }
}

function renderBetScreen(main, playerId) {
    let bet = 0;
    clearTimeout(feedbackTimer);
    clearTimeout(botFeedbackTimer);
    document.querySelector(".px-feedback-backdrop")?.remove();
    main.innerHTML = `
        <section class="px-bet-screen" aria-labelledby="px-bet-title">
            <p class="px-kicker">Peixinho</p>
            <h1 id="px-bet-title">Escolhe a tua aposta</h1>
            <p class="px-subtitle">Cada chip soma ao valor com que te sentas à mesa.</p>
            <div class="bj-chips px-chips" aria-label="Chips de aposta">
                ${CHIPS.map((chip) => `
                    <button class="bj-chip px-chip" type="button" data-chip="${chip.value}" aria-label="Adicionar ${chip.value} à aposta">
                        <img src="${chip.image}" alt="Chip de ${chip.value}">
                    </button>
                `).join("")}
            </div>
            <p class="px-bet-total">Aposta: <strong id="px-bet-amount">0</strong></p>
            <button id="px-start" class="px-button" type="button" disabled>Sentar à mesa</button>
            <p id="px-bet-error" class="px-error" role="alert"></p>
        </section>
    `;

    const amount = document.querySelector("#px-bet-amount");
    const start = document.querySelector("#px-start");
    document.querySelectorAll(".px-chip").forEach((chip) => {
        chip.addEventListener("click", () => {
            bet += Number(chip.dataset.chip);
            amount.textContent = bet;
            start.disabled = bet <= 0;
        });
    });
    start.addEventListener("click", async () => {
        start.disabled = true;
        try {
            rememberBet(bet);
            const state = await api("peixinho/start", {
                method: "POST",
                body: JSON.stringify({ playerId, bet }),
            });
            renderGame(main, state);
        } catch (error) {
            document.querySelector("#px-bet-error").textContent = error.message;
            start.disabled = false;
        }
    });
}

function renderError(main, state, error) {
    renderGame(main, state);
    const errorBox = document.createElement("p");
    errorBox.className = "px-error";
    errorBox.setAttribute("role", "alert");
    errorBox.textContent = error.message;
    document.querySelector(".px-request").prepend(errorBox);
}

async function loadExistingGame(playerId) {
    return api(`peixinho/state/${playerId}`);
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

    main.innerHTML = `<p class="px-loading" role="status">A procurar uma mesa…</p>`;
    loadExistingGame(playerId)
        .then((state) => renderGame(main, state))
        .catch((error) => {
            if (error.status === 404) renderBetScreen(main, playerId);
            else main.innerHTML = `<p class="px-error" role="alert">Não foi possível carregar a mesa: ${error.message}</p>`;
        });
}
