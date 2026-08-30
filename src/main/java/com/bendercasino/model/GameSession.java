package com.bendercasino.model;

import java.util.UUID;

public class GameSession {

    private final UUID gameId;
    private final UUID playerId;
    private final String deckId;
    private final String game;
    private Bet bet;
    private GameStatus status;
    private Object state;

    public GameSession(UUID playerId, String deckId, String game, int betAmount) {
        this.gameId     = UUID.randomUUID();
        this.playerId   = playerId;
        this.deckId     = deckId;
        this.game       = game;
        this.bet        = new Bet(betAmount);
        this.status     = GameStatus.PLAYER_TURN;
        this.state      = null;
    }


    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    public UUID getGameId()         { return gameId; }
    public UUID getPlayerId()       { return playerId; }
    public String getDeckId()       { return deckId; }
    public String getGame()         { return game; }
    public Bet getBet()             { return bet; }
    public GameStatus getStatus()   { return status; }
    public Object getState()        { return state; }

    public void setBet(Bet bet)             { this.bet     = bet; }
    public void setStatus(GameStatus s)     { this.status  = s; }
    public void setState(Object state)      { this.state   = state; }
}
