import { getPlayerById } from "../../js/services/player-service.js"


export async function init() {
    document.querySelector("main").innerHTML = `
        <section class="profile">
            <h2>Profile</h2>
            <div class="profile-grid">
                <article class="profile-card">
                    <p class="profile-label">Name</p>
                    <p class="profile-value" id="profile-name">—</p>
                </article>
                <article class="profile-card">
                    <p class="profile-label">Balance</p>
                    <p class="profile-value" id="profile-balance">—</p>
                </article>
            </div>
            <div class="stats">
                <article class="profile-card">
                    <p class="profile-label">Wins</p>
                    <p class="profile-value" id="stat-wins">—</p>
                </article>
                <article class="profile-card">
                    <p class="profile-label">Losses</p>
                    <p class="profile-value" id="stat-losses">—</p>
                </article>
                
            </div>
            <button class="btn" type="button" id="profile-reset">Reset</button>
        </section>
    `;

    const statWins = document.querySelector("#stat-wins");
    const statLosses = document.querySelector("#stat-losses");
    const name = document.querySelector("#profile-name");
    const balance = document.querySelector("#profile-balance");


    const player = await getPlayerById(sessionStorage.getItem("playerId"));

    console.log(player);

    statWins.textContent = player.stats.wins;
    statLosses.textContent = player.stats.losses;
    name.textContent = player.name;
    balance.textContent = player.balance;


}
