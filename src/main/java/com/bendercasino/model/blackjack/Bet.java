package com.bendercasino.model.blackjack;


public record Bet(int amount, int payout) {

    public Bet(int amount) {
        this(amount, 0);
    }

    public Bet withPayout(int payout) {
        return new Bet(this.amount, payout);
    }
}
