package com.bendercasino.model;

import java.util.ArrayList;
import java.util.List;

public class VideoPokerState {

    private List<Card> cards;
    private PokerHandCategory category;

    public VideoPokerState(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        this.category = null;
    }

    public List<Card> getCards() {
        return cards;
    }

    public PokerHandCategory getCategory() {
        return category;
    }

    public void setCards(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void setCategory(PokerHandCategory category) {
        this.category = category;
    }
}
