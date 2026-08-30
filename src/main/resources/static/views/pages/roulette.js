import state from "/js/state.js";
import { API_BASE_URL } from "/js/constants/utils.js";

const EUROPEAN_ORDER = [
    0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11,
    30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18,
    29, 7, 28, 12, 35, 3, 26
];
const SECTOR = 360 / 37;

const WHEEL_OFFSET = 0;

/** @type {"RED"|"BLACK"|"ODD"|"EVEN"|"LOW"|"HIGH"|null} */

let selectedBetType = null;
let currentBet = 0;

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
            selectedBetType = btn.dataset.bettype;
            const labels = { RED: "Red", BLACK: "Black", ODD: "Odd", EVEN: "Even", LOW: "1 to 18", HIGH: "19 to 36" };
            document.getElementById("bet-selected").textContent = `Bet: ${labels[selectedBetType]}`;
            document.getElementById("roulette-error").textContent = "";
        });
    });

    document.querySelectorAll(".chip").forEach(chip => {
        chip.addEventListener("click", () => {
            const value = parseInt(chip.dataset.value, 10);
            const player = state.getPlayer();
            if (currentBet + value > parseInt(player.balance, 10)) {
                document.getElementById("roulette-error").textContent = "Not enough chips.";
                return;
            }
            currentBet += value;
            document.getElementById("bet-amount-display").textContent = currentBet;
            document.getElementById("roulette-error").textContent = "";
        });
    });

    document.getElementById("clear-bet-btn").addEventListener("click", () => {
        currentBet = 0;
        document.getElementById("bet-amount-display").textContent = 0;
    });

    document.getElementById("spin-btn").addEventListener("click", onSpinClick);
    document.getElementById("back-to-table-btn").addEventListener("click", showTableView);
}

async function onSpinClick() {
    const errorEl = document.getElementById("roulette-error");
    const amount = currentBet;
    const player = state.getPlayer();

    errorEl.textContent = "";

    if (!selectedBetType) {
        errorEl.textContent = "Choose bet type on the table.";
        return;
    }
    if (!amount || amount <= 0) {
        errorEl.textContent = "Place a bet first.";
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
    const result = await callRouletteApi(player.playerId, amount, selectedBetType);

    await transitionToWheel();

    spinWheelTo(result.number);
    await wait(4200);

    showResult(result);
    updateBalance(result.balance);
    currentBet = 0;
    document.getElementById("bet-amount-display").textContent = 0;
} catch (err) {
   
    showTableView();
    errorEl.textContent = err.message || "Something went wrong.";
} finally {

    spinBtn.disabled = false;
    spinBtn.textContent = "SPIN";
}
}

async function callRouletteApi(playerId, betAmount, betType) {
    const response = await fetch(`${API_BASE_URL}roulette/spin`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            playerId: playerId,
            bet: betAmount,
            betType: betType
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