import router from "../../router.js";
import state from "/js/state.js";
import { CHIPS } from "../../js/blackjack/chips.js";
import { roll } from "../../js/services/slots-service.js";

const SYMBOL_IMAGES = {
    CHERRY: "/assets/SlotsImages/Cherry.png?v=3",
    BAR: "/assets/SlotsImages/BAR.png?v=3",
    DOUBLE_BAR: "/assets/SlotsImages/DOUBLE_BAR.png",
    TRIPLE_BAR: "/assets/SlotsImages/TRIPLE_BAR.png",
    BELL: "/assets/SlotsImages/Bell.png?v=3",
    LEELA: "/assets/SlotsImages/Leela.png",
    FRY: "/assets/SlotsImages/Fry.png",
    BENDER: "/assets/SlotsImages/Bender.png?v=4",
};

const SYMBOLS = Object.keys(SYMBOL_IMAGES);

function reelWindow(index) {
    return `
        <div class="slots-reel" data-reel="${index}">
            <img class="slots-symbol-img" alt="" hidden>
            <span class="slots-symbol-label">—</span>
        </div>
    `;
}

function showSymbol(reel, symbol) {
    const img = reel.querySelector(".slots-symbol-img");
    const label = reel.querySelector(".slots-symbol-label");
    const src = SYMBOL_IMAGES[symbol];

    if (src) {
        img.src = src;
        img.alt = symbol.replaceAll("_", " ");
        img.hidden = false;
        label.hidden = true;
        return;
    }

    img.removeAttribute("src");
    img.alt = "";
    img.hidden = true;
    label.hidden = false;
    label.textContent = !symbol || symbol === "NONE" ? "—" : symbol.replaceAll("_", " ");
}

function wait(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

function startReelSpin(reel) {
    reel.classList.add("spinning");
    return setInterval(() => {
        showSymbol(reel, SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)]);
    }, 70);
}

function stopReel(timer, reel, symbol) {
    clearInterval(timer);
    reel.classList.remove("spinning");
    showSymbol(reel, symbol);
}

export function init() {
    const main = document.querySelector("main");
    const player = state.getPlayer();

    if (!player.playerId) {
        main.innerHTML = `
            <section class="join">
                <h2>No player yet</h2>
                <p>Create a player before pulling the lever.</p>
                <a id="go-home" href="/" class="btn">Go back</a>
            </section>
        `;
        document.querySelector("#go-home").addEventListener("click", (event) => {
            event.preventDefault();
            router.navigate("/");
        });
        return;
    }

    main.innerHTML = `
        <section class="slots">
            <h2>Slots</h2>
            <div class="slots-floor">
                <div id="chips" class="bj-chips">
                    ${CHIPS.map(
                        (chip) => `
                    <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                        <img src="${chip.image}" alt="${chip.value} dollar chip">
                    </button>
                    `,
                    ).join("")}
                </div>
                <div class="slots-machine">
                    <div class="slots-readout">
                        <p>Balance <span id="slots-balance">${player.balance ?? "—"}</span></p>
                        <p>Bet <span id="slots-bet">0</span> <button type="button" class="btn-clear-bet" id="slots-clear-bet">✕ Clear</button></p>
                    </div>
                    <div class="slots-window" id="slots-window">
                        ${reelWindow(0)}
                        ${reelWindow(1)}
                        ${reelWindow(2)}
                    </div>
                    <p class="slots-result" id="slots-result">Pick a bet. Then spin.</p>
                    <button type="button" id="slots-spin">
                        <span class="button_top">Spin</span>
                    </button>
                </div>
            </div>
        </section>
    `;

    const betEl = document.querySelector("#slots-bet");
    const balanceEl = document.querySelector("#slots-balance");
    const resultEl = document.querySelector("#slots-result");
    const spinBtn = document.querySelector("#slots-spin");
    const reels = document.querySelectorAll(".slots-reel");
    const chips = document.querySelectorAll(".bj-chip");
    let bet = 0;
    let spinning = false;

    chips.forEach((chip) => {
        chip.addEventListener("click", () => {
            if (spinning) {
                return;
            }
            bet += parseInt(chip.dataset.chip, 10);
            betEl.textContent = String(bet);
        });
    });

    document.querySelector("#slots-clear-bet").addEventListener("click", () => {
        if (spinning) {
            return;
        }
        bet = 0;
        betEl.textContent = String(bet);
    });

    spinBtn.addEventListener("click", async () => {
        if (spinning) {
            return;
        }
        if (bet <= 0) {
            resultEl.textContent = "Pick a bet first.";
            return;
        }

        const current = state.getPlayer();
        const balance = Number(current.balance);
        if (balance < bet) {
            resultEl.textContent = "Not enough balance.";
            return;
        }

        spinning = true;
        spinBtn.disabled = true;
        chips.forEach((chip) => {
            chip.disabled = true;
        });
        resultEl.textContent = "Spinning...";

        const timers = [...reels].map(startReelSpin);

        try {
            const result = await roll(current.playerId, bet);
            const landed = result.symbols || [];

            for (let i = 0; i < reels.length; i++) {
                await wait(350);
                stopReel(timers[i], reels[i], landed[i] || "NONE");
            }

            const nextBalance = result.balance ?? (balance - bet + result.payout);
            balanceEl.textContent = String(nextBalance);
            state.setPlayer({
                playerId: current.playerId,
                name: current.playerName,
                balance: nextBalance,
            });
            if (result.outcome === "CONSOLATION") {
                resultEl.textContent = `Close... paid ${result.payout}`;
            } else if (result.outcome === "WIN") {
                resultEl.textContent = `WIN · For now... ${result.payout}`;
            } else {
                resultEl.textContent = `LOSS · Try Again`;
            }
        } catch (error) {
            timers.forEach((timer, index) => stopReel(timer, reels[index], "—"));
            const message = error.message || "Spin failed.";
            resultEl.textContent = message;
            console.error(error);
            if (message.includes("401")) {
                state.clearPlayer();
                resultEl.textContent = "Session expired. Log in again.";
                router.navigate("/");
            }
        } finally {
            spinning = false;
            spinBtn.disabled = false;
            chips.forEach((chip) => {
                chip.disabled = false;
            });
        }
    });
}
