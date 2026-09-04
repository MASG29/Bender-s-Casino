import { getPlayerById } from "../../js/services/player-service.js"
import state from "/js/state.js";

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
                <article class="profile-card">
                    <p class="profile-label">Games Played</p>
                    <p class="profile-value" id="stat-games">—</p>
                </article>
                <article class="profile-card">
                    <p class="profile-label">Win Rate</p>
                    <p class="profile-value" id="stat-winrate">—</p>
                </article>
            </div>
            <button class="btn" type="button" id="profile-reset">Reset Balance</button>
            <p class="modal-error" id="reset-msg" style="margin-top: 0.75rem;"></p>
        </section>
    `;

    const playerId = sessionStorage.getItem("playerId");

    let player;
    try {
        player = await getPlayerById(playerId);
    } catch (err) {
        console.error("Failed to load player:", err);
        document.querySelector("#reset-msg").textContent = "Failed to load profile. Try logging in again.";
        document.querySelector("#reset-msg").style.color = "#c0392b";
        return;
    }

    const wins    = player.stats.wins   ?? 0;
    const losses  = player.stats.losses ?? 0;
    const games   = wins + losses;
    const winRate = games > 0 ? ((wins / games) * 100).toFixed(1) + "%" : "N/A";

    document.querySelector("#profile-name").textContent    = player.name;
    document.querySelector("#profile-balance").textContent = player.balance + " chips";
    document.querySelector("#stat-wins").textContent       = wins;
    document.querySelector("#stat-losses").textContent     = losses;
    document.querySelector("#stat-games").textContent      = games;
    document.querySelector("#stat-winrate").textContent    = winRate;

    document.querySelector("#profile-reset").addEventListener("click", async () => {
        const msgEl = document.querySelector("#reset-msg");
        msgEl.style.color = "";
        msgEl.textContent = "Resetting...";
        try {
            const res = await fetch(`/api/players/${player.playerId}/reset`, { method: "POST" });
            if (!res.ok) throw new Error("Reset failed");
            const updated = await res.json();
            document.querySelector("#profile-balance").textContent = updated.balance + " chips";
            state.updateHeader();
            msgEl.style.color = "#00C896";
            msgEl.textContent = "Balance reset successfully!";
        } catch (err) {
            msgEl.style.color = "#c0392b";
            msgEl.textContent = err.message || "Something went wrong.";
        }
    });
}