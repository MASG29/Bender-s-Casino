import router from "/router.js";
import state from "/js/state.js";

const BENDER_JOKES = [
    "I was gonna go to law school, but that seemed like a lot of work, so I got a robot to bend the odds instead.",
    "Bite my shiny metal odds.",
    "Robots don't have a heart to break, but I do have a chip you can lose.",
    "Kill all humans? Nah, just their bankrolls.",
    "I'm 40% chrome, 60% grudge against the house — wait, I am the house.",
    "New junk? No. New chips. Sit down, meatbag.",
    "Ah, gambling. Not a good hobby, but I'm a horrible influence, so let's do it.",
    "I once lost a planet in a card game. This table's low stakes by comparison.",
    "Whisky. Cards. And whatever passes for luck around here.",
    "I don't drink, but I know a robot who does.",
];

function randomJoke() {
    return BENDER_JOKES[Math.floor(Math.random() * BENDER_JOKES.length)];
}

export function init() {
    const loggedIn = state.isLoggedIn();
    const player = state.getPlayer();

    const joinMarkup = loggedIn
        ? `
    <section class="join" id="join">
      <h2>Welcome, ${player.playerName} to Bender's Casino</h2>
      <p>"${randomJoke()}"</p>
      <a href="#" class="btn" id="go-lobby-btn">Go To Lobby</a>
    </section>
    `
        : `
    <section class="join" id="join">
      <h2>Membership Has Its Dents</h2>
      <p>Sign up for a starting stack and a dealer who remembers every bet you've lost.</p>
      <a href="#" class="btn" id="open-modal-btn">Pick A Name</a>
    </section>
    `;

    document.querySelector("main").innerHTML = `
    ${joinMarkup}

    <section class="hero">
      <div class="hero-image">
        <img class="bender-img" src="/assets/Bender.png" alt="Robot casino dealer mascot">
      </div>
      <h1>The House Always Bends The Rules</h1>
      <div class="hero-text">
        <p>A casino floor run by a chrome-plated dealer with no interest in your winning streak.</p>
      </div>
    </section>

    ${loggedIn ? "" : `
    <div class="modal-backdrop" id="login-modal">
  <div class="modal-box">
    <button class="modal-close" id="modal-close-btn">✕</button>

    <div class="modal-tabs">
      <button class="modal-tab active" id="tab-login">Login</button>
      <button class="modal-tab" id="tab-register">New Account</button>
    </div>

    <div id="form-login">
      <h2 class="modal-title">Welcome back, meat bag.</h2>
      <p class="modal-hint">Nickname or email. We don't judge.</p>
      <input id="login-identifier" class="modal-input" type="text" maxlength="60" placeholder="Nickname or email..." autocomplete="off" />
      <input id="login-password" class="modal-input" type="password" maxlength="50" placeholder="Password..." autocomplete="off" style="margin-top:10px;" />
    </div>

    <div id="form-register" style="display:none;">
      <h2 class="modal-title">New here? Good luck.</h2>
      <p class="modal-hint">Fill in the details. Bender will forget them immediately.</p>
      <div style="display:flex; gap:10px;">
        <input id="reg-firstname" class="modal-input" type="text" maxlength="30" placeholder="First name..." autocomplete="off" />
        <input id="reg-lastname"  class="modal-input" type="text" maxlength="30" placeholder="Last name..."  autocomplete="off" />
      </div>
      <input id="reg-nickname" class="modal-input" type="text"     maxlength="30" placeholder="Nickname..."  autocomplete="off" style="margin-top:10px;" />
      <input id="reg-email"    class="modal-input" type="email"    maxlength="60" placeholder="Email..."     autocomplete="off" style="margin-top:10px;" />
      <input id="reg-password" class="modal-input" type="password" maxlength="50" placeholder="Password..."  autocomplete="off" style="margin-top:10px;" />
      <input id="reg-confirm"  class="modal-input" type="password" maxlength="50" placeholder="Confirm password..." autocomplete="off" style="margin-top:10px;" />
    </div>

    <p class="modal-error" id="login-error"></p>

    <button class="modal-submit" id="login-submit">
      <span class="btn-label">Login</span>
    </button>
  </div>
</div>
    `}
    `;

    if (loggedIn) {
        document.getElementById("go-lobby-btn").addEventListener("click", (e) => {
            e.preventDefault();
            router.navigate("/lobby");
        });
        return;
    }

    document.getElementById("open-modal-btn").addEventListener("click", function(e) {
        e.preventDefault();
        document.getElementById("login-modal").classList.add("open");
        setTimeout(() => document.getElementById("login-identifier").focus(), 50);
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

    document.getElementById("login-identifier").addEventListener("keydown", (e) => {
    if (e.key === "Enter") submitLogin();
});
document.getElementById("login-password").addEventListener("keydown", (e) => {
    if (e.key === "Enter") submitLogin();
});
}


let currentMode = "login";

function setMode(mode) {
    currentMode = mode;
    const isLogin = mode === "login";

    document.getElementById("tab-login").classList.toggle("active", isLogin);
    document.getElementById("tab-register").classList.toggle("active", !isLogin);

    document.getElementById("form-login").style.display    = isLogin ? "block" : "none";
    document.getElementById("form-register").style.display = isLogin ? "none"  : "block";

    document.getElementById("login-submit").querySelector(".btn-label").textContent = isLogin
        ? "Login"
        : "Create Account";

    document.getElementById("login-error").textContent = "";
}
async function submitLogin() {
    const btn = document.getElementById("login-submit");
    const errorEl = document.getElementById("login-error");
    errorEl.textContent = "";

    if (currentMode === "login") {
        const identifier = document.getElementById("login-identifier").value.trim();
        const password   = document.getElementById("login-password").value.trim();

        if (!identifier) { errorEl.textContent = "Nickname or email is required."; return; }
        if (!password)   { errorEl.textContent = "Password is required."; return; }

        btn.disabled = true;
        btn.querySelector(".btn-label").textContent = "Loading...";

        

         try {
             const res = await fetch("/api/auth/login", {
                 method : "POST",
                 headers: { "Content-Type": "application/json" },
                 body   : JSON.stringify({ identifier, password }),
             });
             if (!res.ok) {
                 const err = await res.json().catch(() => ({}));
                 throw new Error(err.message || `Erro ${res.status}`);
             }
             const player = await res.json();
             state.setPlayer(player);
             state.updateHeader();
             document.getElementById("login-modal").classList.remove("open");
             router.navigate("/lobby");
         } catch (err) {
             errorEl.textContent = err.message || "Something went wrong.";
             btn.disabled = false;
             btn.querySelector(".btn-label").textContent = "Login";
         }

    } else {
        const firstName = document.getElementById("reg-firstname").value.trim();
        const lastName  = document.getElementById("reg-lastname").value.trim();
        const nickname  = document.getElementById("reg-nickname").value.trim();
        const email     = document.getElementById("reg-email").value.trim();
        const password  = document.getElementById("reg-password").value.trim();
        const confirm   = document.getElementById("reg-confirm").value.trim();

        if (!firstName) { errorEl.textContent = "First name is required.";           return; }
        if (!lastName)  { errorEl.textContent = "Last name is required.";            return; }
        if (!nickname)  { errorEl.textContent = "Nickname is required.";             return; }
        if (!email || !email.includes("@")) { errorEl.textContent = "Valid email is required."; return; }
        if (!password)  { errorEl.textContent = "Password is required.";             return; }
        if (password.length < 6) { errorEl.textContent = "Password must be at least 6 characters."; return; }
        if (password !== confirm) { errorEl.textContent = "Passwords do not match."; return; }

        btn.disabled = true;
        btn.querySelector(".btn-label").textContent = "Loading...";

      

         try {
             const res = await fetch("/api/auth/register", {
                 method : "POST",
                 headers: { "Content-Type": "application/json" },
                 body: JSON.stringify({
                    name     : nickname,
                    username : nickname,
                    firstName: firstName,
                    lastName : lastName,
                    email    : email,
                    password : password,
                }),
             });
             if (!res.ok) {
                 const err = await res.json().catch(() => ({}));
                 throw new Error(err.message || `Erro ${res.status}`);
             }
             const player = await res.json();
             state.setPlayer(player);
             state.updateHeader();
             document.getElementById("login-modal").classList.remove("open");
             router.navigate("/lobby");
         } catch (err) {
             errorEl.textContent = err.message || "Something went wrong.";
             btn.disabled = false;
             btn.querySelector(".btn-label").textContent = "Create Account";
        }
    }
}