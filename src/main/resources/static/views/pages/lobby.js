import router from "../../router.js";
import { gamesList } from "../../routes.js";

function tableCard(game) {
    const meta = game.live ? "Open" : "Coming soon";
    const statusClass = game.live ? "table-live" : "table-soon";

    return [
        `                <a class="table ${statusClass}" href="/${game.id}" data-table="${game.id}">`,
        `                    <span class="table-game">${game.label}</span>`,
        `                    <span class="table-meta">${meta}</span>`,
        `                </a>`,
    ].join("\n");
}

export function init() {
    document.querySelector("main").innerHTML = `
        <section class="lobby">
            <h2>Pick A Table</h2>
            <p>Only one game is live. The rest are still warming up the chrome.</p>
            <div class="tables">
${gamesList.map(tableCard).join("\n")}
            </div>
        </section>
    `;

    gamesList.filter((game) => game.live).forEach((game) => {
        document.querySelector(`[data-table=${game.id}]`).addEventListener("click", (event) => {
            event.preventDefault();
            router.navigate(`/${game.id}`);
        });
    });
}
