package com.bendercasino.model.videopoker;

public class PokerPaytable {

    private PokerPaytable() {}

    public static int payout(PokerHandCategory category, int bet) {
        int multiplier = switch (category) {
            case ROYAL_FLUSH -> 250;
            case STRAIGHT_FLUSH -> 50;
            case FOUR_OF_A_KIND -> 25;
            case FULL_HOUSE -> 9;
            case FLUSH -> 6;
            case STRAIGHT -> 4;
            case THREE_OF_A_KIND -> 3;
            case TWO_PAIR -> 2;
            case JACKS_OR_BETTER -> 1;
            case NOTHING -> 0;
        };
        return multiplier * bet;
    }
}
