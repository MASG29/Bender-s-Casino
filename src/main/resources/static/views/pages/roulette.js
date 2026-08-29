import state from "/js/state.js";
import { API_BASE_URL } from "/js/constants/utils.js";

const EUROPEAN_ORDER = [
    0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11,
    30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18,
    29, 7, 28, 12, 35, 3, 26
];
const SECTOR = 360 / 37;

const WHEEL_OFFSET = 0;

/** @type {"RED"|"BLACK"|null} */
let selectedColour = null;

export function init() {
    const main = document.querySelector("main");
    const balance = state.getPlayer().balance ?? 0;

    main.innerHTML = `
        <div class="roulette-page">

            <!-- VISTA 1: MESA (apostas apenas black ou red depois mudamos) -->
            <div class="table-view" id="table-view">
                <div class="roulette-balance">
                    <span>Balance</span>
                    <span id="balance-display">${balance} chips</span>
                </div>

                <div class="table-img-wrap">
                    <img src="/assets/roulette/rouletteTable.png" alt="Roulette table" />
                    <button type="button" class="bet-zone zone-red" data-colour="RED" aria-label="Bet Red"></button>
                    <button type="button" class="bet-zone zone-black" data-colour="BLACK" aria-label="Bet Black"></button>
                </div>

                <div class="roulette-bet-area">
                    <label for="bet-amount">Bet amount</label>
                    <input id="bet-amount" class="modal-input" type="number" min="1" placeholder="0" />
                </div>

                <p class="roulette-bet-selected" id="bet-selected">No bet selected</p>
                <p class="modal-error" id="roulette-error"></p>

                <button class="btn roulette-spin-btn" id="spin-btn">SPIN</button>
            </div>

            <!-- VISTA 2: ROLETA (depois do spin) -->
            <div class="wheel-view" id="wheel-view">
                <div class="wheel-pointer"></div>
                <img class="wheel-spin" id="wheel-img"
                     src="/assets/roulette/roulette.png"
                     alt="Roulette wheel" />
                <p class="roulette-result" id="roulette-result"></p>
                <button class="btn" id="back-to-table-btn">Nova aposta</button>
            </div>

        </div>
    `;

    setupGame();
}

function setupGame() {
    document.querySelectorAll(".bet-zone").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".bet-zone").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            selectedColour = btn.dataset.colour; // "RED" | "BLACK"
            document.getElementById("bet-selected").textContent = `Bet: ${selectedColour}`;
            document.getElementById("roulette-error").textContent = "";
        });
    });

    document.getElementById("spin-btn").addEventListener("click", onSpinClick);
    document.getElementById("back-to-table-btn").addEventListener("click", showTableView);
}

async function onSpinClick() {
    const errorEl = document.getElementById("roulette-error");
    const amount = parseInt(document.getElementById("bet-amount").value, 10);
    const player = state.getPlayer();

    errorEl.textContent = "";

    if (!selectedColour) {
        errorEl.textContent = "Choose Red or Black on the table.";
        return;
    }
    if (!amount || amount <= 0) {
        errorEl.textContent = "Enter a valid amount.";
        return;
    }
    if (amount > parseInt(player.balance, 10)) {
        errorEl.textContent = "Not enough chips.";
        return;
    }
    if (!player.playerId) {
        errorEl.textContent = "Not logged in.";
        return;
    }

    const spinBtn = document.getElementById("spin-btn");
    spinBtn.disabled = true;
    spinBtn.textContent = "Spinning...";

    try {
    const result = await callRouletteApi(player.playerId, amount, selectedColour);

    await transitionToWheel();

    spinWheelTo(result.number);
    await wait(4200);

    showResult(result);
    updateBalance(result.balance);
} catch (err) {
   
    showTableView();
    errorEl.textContent = err.message || "Something went wrong.";
} finally {

    spinBtn.disabled = false;
    spinBtn.textContent = "SPIN";
}
}

async function callRouletteApi(playerId, betAmount, colour) {
    const response = await fetch(`${API_BASE_URL}roulette/spin`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            playerId: playerId,
            bet: betAmount,
            colour: colour
        })
    });

    if (!response.ok) {
        let msg = "Spin failed";
        try {
            const text = await response.text();
            if (text) msg = text;
        } catch (_) { /* ignore */ }
        throw new Error(msg);
    }

    return response.json();
}

function angleForNumber(n) {
    const index = EUROPEAN_ORDER.indexOf(n);
    if (index < 0) return 0;
    return index * SECTOR + WHEEL_OFFSET;
}

function spinWheelTo(number) {
    const wheel = document.getElementById("wheel-img");
    const baseTurns = 5;
    const targetAngle = angleForNumber(number);
    const finalDeg = baseTurns * 360 + (360 - targetAngle);

    wheel.style.transition = "none";
    wheel.style.transform = "rotate(0deg)";
    void wheel.offsetWidth;

    wheel.style.transition = "transform 4s cubic-bezier(0.12, 0.8, 0.2, 1)";
    wheel.style.transform = `rotate(${finalDeg}deg)`;
}

async function transitionToWheel() {
    const tableView = document.getElementById("table-view");
    const wheelView = document.getElementById("wheel-view");

    document.getElementById("roulette-result").textContent = "";

    
    tableView.classList.add("zoom-fade-out");
    await wait(800);

    
    tableView.style.display = "none";
    tableView.classList.remove("zoom-fade-out");

    wheelView.style.display = "flex";
    wheelView.classList.add("visible");
    
    wheelView.classList.remove("fade-zoom-in");
    void wheelView.offsetWidth;

    wheelView.classList.add("fade-zoom-in");
    await wait(800);
}

function showTableView() {
    const tableView = document.getElementById("table-view");
    const wheelView = document.getElementById("wheel-view");

    wheelView.classList.remove("fade-zoom-in", "visible");
    wheelView.style.display = "none";

    tableView.style.display = "flex";
    tableView.classList.remove("zoom-fade-out");
    document.getElementById("roulette-error").textContent = "";
}

function showResult(result) {
    const resultEl = document.getElementById("roulette-result");
    const colourLabel =
        result.colour === "GREEN" ? "🟢 GREEN (0 — house wins)"
        : result.colour === "RED" ? "🔴 RED"
        : "⚫ BLACK";

    if (result.won) {
        resultEl.textContent = `${result.number} ${colourLabel} — +${result.payout} chips`;
        resultEl.style.color = "#00C896";
    } else {
        resultEl.textContent = `${result.number} ${colourLabel} — you lose`;
        resultEl.style.color = "#c0392b";
    }
}

function updateBalance(balance) {
    sessionStorage.setItem("balance", String(balance));
    document.getElementById("balance-display").textContent = `${balance} chips`;
}

function wait(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}