const state = {
    getPlayer() {
        return {
            playerId    : sessionStorage.getItem("playerId"),
            playerName  : sessionStorage.getItem("playerName"),
            balance     : sessionStorage.getItem("balance"),
        };
    },

    setPlayer(player) {
        sessionStorage.setItem("playerId",   player.playerId);
        sessionStorage.setItem("playerName", player.name);
        sessionStorage.setItem("balance",    player.balance);
        this.updateHeader();
    },

    clearPlayer() {
        sessionStorage.removeItem("playerId");
        sessionStorage.removeItem("playerName");
        sessionStorage.removeItem("balance");
    },

    isLoggedIn() {
        return !!sessionStorage.getItem("playerId");
    },

    updateHeader() {
        const logoutBtn = document.getElementById("logout-btn");
        const balanceEl = document.getElementById("header-balance");
        const loggedIn = this.isLoggedIn();

        if (logoutBtn) logoutBtn.style.display = loggedIn ? "block" : "none";
        if (balanceEl) {
            balanceEl.style.display = loggedIn ? "inline" : "none";
            balanceEl.textContent = `${sessionStorage.getItem("balance") ?? 0} chips`;
        }
    }
}
export default state;