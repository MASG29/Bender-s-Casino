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
                            <button class="bet-opt" data-bet="red">🔴 Red</button>
                            <button class="bet-opt" data-bet="black">⚫ Black</button>
                            <button class="bet-opt" data-bet="even">Even</button>
                            <button class="bet-opt" data-bet="odd">Odd</button>
                            <button class="bet-opt" data-bet="1-18">1-18</button>
                            <button class="bet-opt" data-bet="19-36">19-36</button>
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
        game.classList.add("fade.in");
    }, 3000)
}

// game UI

let selectedBet = null;

function setupGame() {
    document.querySelectorAll(".bet-opt").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".bet-opt").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            selectedBet = btn.dataset.bet;
            document.getElementById("bet-selected").textContent = `Bet: ${selectedBet}`;
            document.getElementById("roulette-error").textContent = "";
        });
    });

    document.getElementById("spin.btn").addEventListener("click", onSpinClick);
}

async function onSpinClick() {
    const errorEl = document.getElementById("roulette-error");
    const amount  = parseInt(document.getElementById("bet-amount").value, 10);
    const player  = state.getPlayer();

    errorEl.textContent = "";

    if (!selectedBet) {
        errorEl.textContent = "Choose a bet first.";
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
    wheel.classList.add("spinning");

    try {
        const result = await callRouletteApi(player.playerId, amount, selectedBet);

        await wait(2000)

        wheel.classList.remove("spinning");
        spinBtn.disabled = false;
        spinBtn.textContentxt = "SPIN";

        showResult(result);
        updateBalance(result.newBalance);
    
    }catch (err) {

        wheel.classList.remove("spinning");
        spinBtn.disabled = false;
        spinBtn.textContent = "SPIN";
        errorEl.textContent = err.message || "Something went wrong.";

    }
}

//MOCK

async function callRouletteApi(playerId, betAmount, betType) {
    // MOCK — só para testar frontend. Apaga quando o backend existir.
    await wait(500);

    const number = Math.floor(Math.random() * 37);
    const red = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];

    let won = false;
    switch (betType) {
        case "red":   won = red.includes(number); break;
        case "black": won = number > 0 && !red.includes(number); break;
        case "even":  won = number > 0 && number % 2 === 0; break;
        case "odd":   won = number > 0 && number % 2 !== 0; break;
        case "1-18":  won = number >= 1 && number <= 18; break;
        case "19-36": won = number >= 19 && number <= 36; break;
    }

    const payout = won ? betAmount * 2 : -betAmount;
    const current = parseInt(sessionStorage.getItem("balance") || "0", 10);
    const newBalance = current + payout;

    return { number, won, payout, newBalance };
}




/*async function callRouletteApi(playerId, betAmount, betType) {
    const response = await fetch(`${API_BASE_URL}games/roulette/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            playerId: playerId,
            bet: betAmount,
            betType: betType
        })
    });

    if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || "Spin failed");
    }

    return response.json();
}*/

function showResult(result) {
    const resultEl = document.getElementById("roulette-result");
    const won = result.won;
    const payout = result.payout;

    resultEl.textContent = `${result.number} — ${won ? `+${payout}` : payout} chips`;
    resultEl.style.color = won ? "#00C896" : "#c0392b";
}

function updateBalance(newBalance) {
    sessionStorage.setItem("balance", newBalance);
    document.getElementById("balance-display").textContent = `${newBalance} chips`;
}

function wait(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
