package com.bendercasino.client;

import com.bendercasino.model.blackjack.Card;
import com.bendercasino.model.Deck;

import java.util.List;

public interface DeckClient {

    Deck newShuffledDeck(int deckCount);

    List<Card> draw(String deckId, int count);
}
