package com.bendercasino.client;

import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "deckofcards.mode", havingValue = "memory")
public class InMemoryDeckClient implements DeckClient {

    private final Map<String, List<Card>> decks = new ConcurrentHashMap<>();

    @Override
    public Deck newShuffledDeck(int deckCount) {
        String deckId = UUID.randomUUID().toString();
        List<Card> cards = buildStandardDeck(deckCount);
        Collections.shuffle(cards);
        decks.put(deckId, new ArrayList<>(cards));
        return new Deck(deckId, cards.size());
    }

    @Override
    public List<Card> draw(String deckId, int count) {
        List<Card> deck = decks.get(deckId);
        if (deck == null || deck.size() < count) {
            throw new com.bendercasino.exception.DeckApiException("deck shuffle" + deckId);
        }
        List<Card> drawn = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();
        return drawn;
    }

    private List<Card> buildStandardDeck(int deckCount) {
        String[] suits  = {"SPADES", "HEARTS", "DIAMONDS", "CLUBS"};
        String[] values = {"2","3","4","5","6","7","8","9","10","JACK","QUEEN","KING","ACE"};
        List<Card> cards = new ArrayList<>();
        for (int d = 0; d < deckCount; d++) {
            for (String suit : suits) {
                for (String value : values) {
                    String code  = value.substring(0, 1) + suit.substring(0, 1);
                    String image = "https://deckofcardsapi.com/static/img/" + code + ".png";
                    cards.add(new Card(code, value, suit, image));
                }
            }
        }
        return cards;
    }
}
