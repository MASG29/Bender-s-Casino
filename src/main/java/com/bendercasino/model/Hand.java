package com.bendercasino.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Hand {

    private final List<Card> cards = new ArrayList<>();

    public void add(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    // --- delegam para HandValueCalculator ---

    public int value() {
        return com.bendercasino.service.HandValueCalculator.value(cards);
    }

    public boolean isSoft() {
        return com.bendercasino.service.HandValueCalculator.isSoft(cards);
    }

    /**
     * Blackjack natural: exactamente 2 cartas com valor total 21.
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && value() == 21;
    }

    public boolean isBusted() {
        return value() > 21;
    }
}
