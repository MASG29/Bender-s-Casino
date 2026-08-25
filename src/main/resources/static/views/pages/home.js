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

        <div class="modal-tabs">
          <button class="modal-tab active" id="tab-login">Login</button>
          <button class="modal-tab" id="tab-register">New Account</button>
        </div>

        <h2 class="modal-title">Welcome back, meat bag?</h2>
        <p class="modal-hint">Enter your credentials to continue.</p>

        <input id="player-name-input" class="modal-input" type="text" maxlength="30" placeholder="Nickname..." autocomplete="off" />
        <input id="player-password-input" class="modal-input" type="password" maxlength="50" placeholder="Password..." autocomplete="off" style="margin-top: 10px;" />
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

    document.getElementById("tab-login").addEventListener("click", () => setMode("login"));
    document.getElementById("tab-register").addEventListener("click", () => setMode("register"));

    document.getElementById("login-submit").addEventListener("click", submitLogin);

    document.getElementById("player-name-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter") submitLogin();
    });
    document.getElementById("player-password-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter") submitLogin();
    });

    if (sessionStorage.getItem("playerId")) {
        router.navigate("/lobby");
    }
}

let currentMode = "login"; // "login" | "register"

function setMode(mode) {
    currentMode = mode;

    const isLogin = mode === "login";

    document.getElementById("tab-login").classList.toggle("active", isLogin);
    document.getElementById("tab-register").classList.toggle("active", !isLogin);

    document.getElementById("modal-title").textContent = isLogin
        ? "Welcome back, meat bag."
        : "New here? Good luck.";

    document.getElementById("modal-hint").textContent = isLogin
        ? "Enter your credentials to continue."
        : "Pick a nickname and password. Don't forget them.";

    document.getElementById("login-submit").querySelector(".btn-label").textContent = isLogin
        ? "Login"
        : "Create Account";

    document.getElementById("login-error").textContent = "";
}

async function submitLogin() {
    const name     = document.getElementById("player-name-input").value.trim();
    const password = document.getElementById("player-password-input").value.trim();

    if (!name)     { document.getElementById("login-error").textContent = "Nickname is required."; return; }
    if (!password) { document.getElementById("login-error").textContent = "Password is required."; return; }

    const btn = document.getElementById("login-submit");
    btn.disabled = true;
    btn.querySelector(".btn-label").textContent = "Loading...";
    document.getElementById("login-error").textContent = "";

    // TODO: apagar mock quando o backend estiver pronto
    const player = { playerId: "mock-id-123", name: name, balance: 1000 };
    sessionStorage.setItem("playerId",   player.playerId);
    sessionStorage.setItem("playerName", player.name);
    sessionStorage.setItem("balance",    player.balance);
    document.getElementById("login-modal").classList.remove("open");
    router.navigate("/lobby");

    // try {
    //     const endpoint = currentMode === "login"
    //         ? "/api/players/login"
    //         : "/api/players";
    //
    //     const res = await fetch(endpoint, {
    //         method : "POST",
    //         headers: { "Content-Type": "application/json" },
    //         body   : JSON.stringify({ name, password }),
    //     });
    //
    //     if (!res.ok) {
    //         const err = await res.json().catch(() => ({}));
    //         throw new Error(err.message || `Erro ${res.status}`);
    //     }
    //
    //     const player = await res.json();
    //     sessionStorage.setItem("playerId",   player.playerId);
    //     sessionStorage.setItem("playerName", player.name);
    //     sessionStorage.setItem("balance",    player.balance);
    //     document.getElementById("login-modal").classList.remove("open");
    //     router.navigate("/lobby");
    //
    // } catch (err) {
    //     document.getElementById("login-error").textContent = err.message || "Something went wrong.";
    //     btn.disabled = false;
    //     btn.querySelector(".btn-label").textContent = currentMode === "login" ? "Login" : "Create Account";
    // }
}