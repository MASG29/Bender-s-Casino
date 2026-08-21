package com.bendercasino.model;

import java.util.UUID;

public class GameSession {

    private final UUID gameId;
    private final UUID playerId;
    private final String deckId;
    private final Hand playerHand;
    private final Hand dealerHand;
    private Bet bet;
    private GameStatus status;
    private Outcome outcome;

    public GameSession(UUID playerId, String deckId, int betAmount) {
        this.gameId     = UUID.randomUUID();
        this.playerId   = playerId;
        this.deckId     = deckId;
        this.playerHand = new Hand();
        this.dealerHand = new Hand();
        this.bet        = new Bet(betAmount);
        this.status     = GameStatus.PLAYER_TURN;
        this.outcome    = null;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    // --- getters / setters ---

    public UUID getGameId()         { return gameId; }
    public UUID getPlayerId()       { return playerId; }
    public String getDeckId()       { return deckId; }
    public Hand getPlayerHand()     { return playerHand; }
    public Hand getDealerHand()     { return dealerHand; }
    public Bet getBet()             { return bet; }
    public GameStatus getStatus()   { return status; }
    public Outcome getOutcome()     { return outcome; }

    public void setBet(Bet bet)             { this.bet     = bet; }
    public void setStatus(GameStatus s)     { this.status  = s; }
    public void setOutcome(Outcome o)       { this.outcome = o; }
}
