package com.bendercasino.client;

import com.bendercasino.exception.DeckApiException;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@ConditionalOnProperty(name = "deckofcards.mode", havingValue = "api", matchIfMissing = true)
public class DeckOfCardsApiClient implements DeckClient {

    private final RestClient restClient;

    public DeckOfCardsApiClient(RestClient.Builder builder
                                 /* @Value("${deckofcards.base-url}") String baseUrl */) {
        // TODO — config RestClient based in base-url and timeouts
        this.restClient = builder.build();
    }

    @Override
    public Deck newShuffledDeck(int deckCount) {
        // TODO — GET /new/shuffle/?deck_count={deckCount}
        throw new UnsupportedOperationException("implement");
    }

    @Override
    public List<Card> draw(String deckId, int count) {
        // TODO — GET /{deckId}/draw/?count={count}
        throw new UnsupportedOperationException("implement");
    }
}
