const CHIPS = [
    { value: 1, image: "/assets/1dollar.coin.png" },
    { value: 5, image: "/assets/5dollarcoin.png" },
    { value: 10, image: "/assets/10dollarcoin.png" },
    { value: 25, image: "/assets/25dollarcoin.png" },
    { value: 50, image: "/assets/50dollarcoin.png" },
    { value: 100, image: "/assets/100dollarcoin.png" },
];

export function init() {
    document.querySelector("main").innerHTML = `
        <section class="bj">
            <h2>Blackjack</h2>

            <div class="bj-floor">
                <div class="bj-chips">
                    ${CHIPS.map(chip => `
                        <button class="bj-chip" type="button" data-chip="${chip.value}" aria-label="Bet ${chip.value}">
                            <img src="${chip.image}" alt="${chip.value} dollar chip">
                        </button>
                    `).join("")}
                </div>

                <div class="bj-table">
                    <div class="bj-seat bj-dealer">
                        <p class="bj-seat-label">Dealer</p>
                        <div class="bj-hand" id="dealer-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-score" id="dealer-score">—</p>
                    </div>

                    <div class="bj-seat bj-player">
                        <p class="bj-score" id="player-score">—</p>
                        <div class="bj-hand" id="player-hand">
                            <div class="bj-slot"></div>
                            <div class="bj-slot"></div>
                        </div>
                        <p class="bj-seat-label">You</p>
                    </div>
                </div>
            </div>

            <div class="blackjack-ui">
                <button type="button" id="bj-hit"><span class="button_top">Hit</span></button>
                <button type="button" id="bj-stand"><span class="button_top">Stand</span></button>
            </div>
        </section>
    `;
}
