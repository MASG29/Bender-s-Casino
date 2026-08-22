package com.bendercasino.client;

import com.bendercasino.client.dto.DeckResponse;
import com.bendercasino.client.dto.DrawResponse;
import com.bendercasino.exception.DeckApiException;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import com.bendercasino.util.CardMapper;
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

        try {
            DeckResponse response = restClient
                    .get()
                    .uri("/new/shuffle/?deck_count={n}", deckCount)
                    .retrieve()
                    .body(DeckResponse.class);

            if (response == null || !response.success()){
                throw new DeckApiException("Fail to create deck");
            }
            return new Deck(response.deck_id(), response.remaining());
        }catch (RestClientException e) {
            throw new DeckApiException("Fail to create deck");
        }
    }


    @Override
    public List<Card> draw(String deckId, int count) {
        // TODO — GET /{deckId}/draw/?count={count}
        try {
            DrawResponse response = restClient
                    .get()
                    .uri("/{id}/draw/?count={n}", deckId, count)
                    .retrieve()
                    .body(DrawResponse.class);

            if (response == null || !response.success()){
                throw new DeckApiException("Fail to draw deck");
            }
            return response.cards()
                    .stream()
                    .map(CardMapper::toDomain)
                    .toList();
        }catch (RestClientException e) {
            throw new DeckApiException("Fail to draw deck");
        }
    }
}
