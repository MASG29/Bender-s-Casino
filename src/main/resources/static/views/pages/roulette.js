import state from "/js/state.js";
import { API_BASE_URL } from "/js/constants/utils.js";

export function init() {
    const main = document.querySelector("main");

    main.innerHTML = `
        <div class="roulette-page">

            <!-- Intro -->
            <div class="roulette-intro" id="roulette-intro">
                <img class="intro-table" id="intro-table"
                     src="/assets/roulette/rouletteTable.png"
                     alt="Roulette Table" />
                <img class="intro-wheel" id="intro-wheel"
                     src="/assets/roulette/roulette.png"
                     alt="Roulette Wheel" />
            </div>

            <!-- game UI hidden  -->
            <div class="roulette-game" id="roulette-game" style="display:none;">
                <div class="roulette-left">
                    <img class="wheel-spin" id="wheel-img"
                         src="/assets/roulette/roulette.png"
                         alt="Roulette Wheel" />
                    <button class="btn roulette-spin-btn" id="spin-btn">SPIN</button>
                    <p class="roulette-result" id="roulette-result"></p>
                </div>

                <div class="roulette-right">
                    <div class="roulette-balance">
                        <span>Balance:</span>
                        <span id="balance-display">${state.getPlayer().balance} chips</span>
                    </div>

                    <div class="roulette-bet-area">
                        <label>Bet amount</label>
                        <input id="bet-amount" class="modal-input" type="number" min="1" placeholder="0" />
                    </div>

                    <div class="roulette-bets">
                        <p class="roulette-bets-title">Choose your bet</p>
                        <div class="bet-options">
                            <button class="bet-opt" data-colour="RED">🔴 Red</button>
                            <button class="bet-opt" data-colour="BLACK">⚫ Black</button>
                        </div>
                    
                        </div>
                    <p class="roulette-bet-selected" id="bet-selected">No bet selected</p>
                    <p class="modal-error" id="roulette-error"></p>
                </div>
            </div>
        </div>
    `;

    runIntroAnimation();
    setupGame();
}

//enter animation

function runIntroAnimation() {
    
    const intro = document.getElementById("roulette-intro");
    const table = document.getElementById("intro-table");
    const wheel = document.getElementById("intro-wheel");
    const game  = document.getElementById("roulette-game");

    setTimeout(() => {
        table.classList.add("fade-in");
    }, 100);

    setTimeout(() =>{
        table.classList.add("zoom-out");
        wheel.classList.add("fade-in");
    }, 1500);

    setTimeout(() => {
        intro.style.display = "none";
        game.style.display = "flex";
        game.classList.add("fade-in");
    }, 3000)
}

// game UI

let selectedColour = null;

function setupGame() {
    document.querySelectorAll(".bet-opt").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".bet-opt").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            selectedColour = btn.dataset.colour; // "RED" | "BLACK"
            document.getElementById("bet-selected").textContent = `Bet: ${selectedColour}`;
            document.getElementById("roulette-error").textContent = "";
        });
    });

    document.getElementById("spin-btn").addEventListener("click", onSpinClick);
}

async function onSpinClick() {
    const errorEl = document.getElementById("roulette-error");
    const amount  = parseInt(document.getElementById("bet-amount").value, 10);
    const player  = state.getPlayer();

    errorEl.textContent = "";

    if (!selectedColour) {
        errorEl.textContent = "Choose a red or black first.";
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

    const spinBtn = document.getElementById("spin-btn");
    spinBtn.disabled = true;
    spinBtn.textContent = "Spinning...";

    const wheel = document.getElementById("wheel-img");
    wheel.classList.remove("spinning");
    void wheel.offsetWidth;
    wheel.classList.add("spinning");

    try {
        const result = await callRouletteApi(player.playerId, amount, selectedColour);

        await wait(2000)

        wheel.classList.remove("spinning");
        spinBtn.disabled = false;
        spinBtn.textContent = "SPIN";

        showResult(result);
        updateBalance(result.balance);
    
    }catch (err) {

        wheel.classList.remove("spinning");
        spinBtn.disabled = false;
        spinBtn.textContent = "SPIN";
        errorEl.textContent = err.message || "Something went wrong.";

    }
}





async function callRouletteApi(playerId, betAmount, colour) {
    const response = await fetch(`${API_BASE_URL}roulette/spin`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            playerId: playerId,
            bet: betAmount,
            colour: colour   // "RED" ou "BLACK"
        })
    });

    if (!response.ok) {
        let msg = "Spin failed";
        try {
            const text = await response.text();
            if (text) msg = text;
        } catch (_) {}
        throw new Error(msg);
    }

    return response.json();
}

function showResult(result) {
    const resultEl = document.getElementById("roulette-result");
    const colourLabel = result.colour === "GREEN"
        ? "🟢 GREEN (0 — house wins)"
        : result.colour === "RED"
            ? "🔴 RED"
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
