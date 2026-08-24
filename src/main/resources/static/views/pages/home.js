import router from "/router.js";

export function init() {
    document.querySelector("main").innerHTML = `
    <section class="join" id="join">
      <h2>Membership Has Its Dents</h2>
      <p>Sign up for a starting stack and a dealer who remembers every bet you've lost.</p>
      <a href="#" class="btn" id="open-modal-btn">Pick A Name</a>
    </section>

    <section class="hero">
      <div class="hero-image">
        <img class="bender-img" src="/assets/Bender.png" alt="Robot casino dealer mascot">
      </div>
      <h1>The House Always Bends The Rules</h1>
      <div class="hero-text">
        <p>A casino floor run by a chrome-plated dealer with no interest in your winning streak.</p>
      </div>
    </section>

    <div class="modal-backdrop" id="login-modal">
      <div class="modal-box">
        <button class="modal-close" id="modal-close-btn">✕</button>
        <h2 class="modal-title">What's Your Name meat bag?</h2>
        <p class="modal-hint">Bender needs a name to insult you correctly.</p>
        <input id="player-nickname-input" class="modal-input" type="text" maxlength="30" placeholder="Your nickname..." autocomplete="off" />
        <input id="player-password-input" class="modal-input" type="password" maxlength="30" placeholder="Your password..." autocomplete="off" />
        <p class="modal-error" id="login-error"></p>
        <button class="modal-submit" id="login-submit">
          <span class="btn-label">Enter the theme park</span>
        </button>
      </div>
    </div>
    `;

    document.getElementById("open-modal-btn").addEventListener("click", function(e) {
        e.preventDefault();
        document.getElementById("login-modal").classList.add("open");
        setTimeout(() => document.getElementById("player-name-input").focus(), 50);
    });

    document.getElementById("modal-close-btn").addEventListener("click", function() {
        document.getElementById("login-modal").classList.remove("open");
    });

    document.getElementById("login-modal").addEventListener("click", (e) => {
        if (e.target === document.getElementById("login-modal")) {
            document.getElementById("login-modal").classList.remove("open");
        }
    });
   
    document.getElementById("login-submit").addEventListener("click", submitLogin);
    document.getElementById("player-name-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            submitLogin();
        }
    });
    if (sessionStorage.getItem("playerId")) {
      router.navigate("/lobby");
    }
}

async function submitLogin() {
    const nicknameInput = document.getElementById("player-nickname-input").value.trim();
    const passwordInput = document.getElementById("player-password-input").value.trim();
    
    if (!nicknameInput) {
        document.getElementById("login-error").textContent = "Please enter a nickname.";
        return;         
    }
    if (!passwordInput) {
        document.getElementById("login-error").textContent = "Please enter a password.";
        return;         
    }

    const btn = document.getElementById("login-submit");
    btn.disabled = true;
    btn.querySelector(".btn-label").textContent = "Entering...";
    document.getElementById("login-error").textContent = "";

    // TODO: apagar mock e descomentar o try/catch quando o backend estiver pronto
    const player = { playerId: "mock-id-123", name: name, balance: 1000 };
    sessionStorage.setItem("playerId",   player.playerId);
    sessionStorage.setItem("playerName", player.name);
    sessionStorage.setItem("balance",    player.balance);
    document.getElementById("login-modal").classList.remove("open");
    router.navigate("/lobby");

    
    // try {
    //     const res = await fetch("/api/players", {
    //         method : "POST",
    //         headers: { "Content-Type": "application/json" },
    //         body   : JSON.stringify({ name }),
    //     });
    //     if (!res.ok) {
    //         const err = await res.json().catch(() => ({}));
    //         throw new Error(err.message || `Erro ${res.status}`);
    //     }
    //     const player = await res.json();
    //     sessionStorage.setItem("playerId",   player.playerId);
    //     sessionStorage.setItem("playerName", player.name);
    //     sessionStorage.setItem("balance",    player.balance);
    //     document.getElementById("login-modal").classList.remove("open");
    //     router.navigate("/lobby");
    // } catch (err) {
    //     document.getElementById("login-error").textContent = err.message || "Something went wrong.";
    //     btn.disabled = false;
    //     btn.querySelector(".btn-label").textContent = "Enter the Casino";
    // }
}