package com.bendercasino.model;

public class BlackjackState {

    private Hand playerHand;
    private Hand dealerHand;
    private Outcome outcome;

    public BlackjackState() {
        this.playerHand = new Hand();
        this.dealerHand = new Hand();
        this.outcome    = null;
    }

    public Hand getPlayerHand()     { return playerHand; }
    public Hand getDealerHand()     { return dealerHand; }
    public Outcome getOutcome()     { return outcome; }

    public void setOutcome(Outcome o) { this.outcome = o; }
}
