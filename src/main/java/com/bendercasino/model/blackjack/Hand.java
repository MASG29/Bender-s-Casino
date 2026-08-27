package com.bendercasino.model.blackjack;

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

    public int value() {
        return com.bendercasino.service.HandValueCalculator.value(cards);
    }

    public boolean isSoft() {
        return com.bendercasino.service.HandValueCalculator.isSoft(cards);
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && value() == 21;
    }

    public boolean isBusted() {
        return value() > 21;
    }
}
