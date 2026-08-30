package com.bendercasino.client;

import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;

import java.util.List;

public interface DeckClient {

    Deck newShuffledDeck(int deckCount);

    List<Card> draw(String deckId, int count);
}
