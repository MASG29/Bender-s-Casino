package com.bendercasino.service.blackjack;

import com.bendercasino.model.Card;

import java.util.List;

public class HandValueCalculator {

    private HandValueCalculator() {}

    public static int value(List<Card> cards) {
        return evaluate(cards)[0];
    }

    public static boolean isSoft(List<Card> cards) {
        return evaluate(cards)[1] > 0;
    }

    private static int[] evaluate(List<Card> cards) {
        int total = 0;
        int liveAces = 0;
        for (Card card : cards) {
            total += card.points();
            if (card.isAce()) {
                liveAces++;
            }
        }
        while (total > 21 && liveAces > 0) {
            total -= 10;
            liveAces--;
        }
        return new int[]{total, liveAces};
    }
}
