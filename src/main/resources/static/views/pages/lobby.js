import router from "../../router.js";

export function init() {
    document.querySelector("main").innerHTML = `
        <section class="lobby">
            <h2>Pick A Table</h2>
            <p>Only one game is live. The rest are still warming up the chrome.</p>
            <div class="tables">
                <a class="table table-live" href="/blackjack" data-table="blackjack">
                    <span class="table-game">Blackjack</span>
                    <span class="table-meta">Open</span>
                </a>
                
            </div>
        </section>
    `;

    document.querySelector("[data-table=blackjack]").addEventListener("click", (event) => {
        event.preventDefault();
        router.navigate("/blackjack");
    });
}
