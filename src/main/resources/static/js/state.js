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
        if (!logoutBtn) return;
        logoutBtn.style.display = this.isLoggedIn() ? "block" : "none";
    }
}
export default state;