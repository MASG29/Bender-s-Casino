package com.bendercasino.client;

import com.bendercasino.exception.DeckApiException;
import com.bendercasino.model.Card;
import com.bendercasino.model.Deck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DeckOfCardsApiClientTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
    private final DeckOfCardsApiClient client = new DeckOfCardsApiClient(builder);

    @Test
    @DisplayName("newShuffledDeck: 200 com JSON válido -> devolve Deck correto")
    void newShuffledDeck_success() {
        String deckId = "abc123";
        int remaining = 52;

        mockServer.expect(requestTo("/new/shuffle/?deck_count=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"success":true,"deck_id":"%s","remaining":%d,"shuffled":true}
                        """.formatted(deckId, remaining), MediaType.APPLICATION_JSON));

        Deck result = client.newShuffledDeck(1);

        assertThat(result).isNotNull();
        assertThat(result.deckId()).isEqualTo(deckId);
        assertThat(result.remaining()).isEqualTo(remaining);
        mockServer.verify();
    }

    @Test
    @DisplayName("newShuffledDeck: 200 com success=false -> lança DeckApiException")
    void newShuffledDeck_successFalse() {
        mockServer.expect(requestTo("/new/shuffle/?deck_count=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"success":false,"deck_id":null,"remaining":0,"shuffled":false}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.newShuffledDeck(1))
                .isInstanceOf(DeckApiException.class)
                .hasMessage("Fail to create deck");
        mockServer.verify();
    }

    @Test
    @DisplayName("newShuffledDeck: 500 erro de rede -> lança DeckApiException")
    void newShuffledDeck_serverError() {
        mockServer.expect(requestTo("/new/shuffle/?deck_count=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.newShuffledDeck(1))
                .isInstanceOf(DeckApiException.class)
                .hasMessage("Fail to create deck");
        mockServer.verify();
    }

    @Test
    @DisplayName("draw: 200 com JSON válido (2 cartas) -> devolve List<Card> mapeada corretamente")
    void draw_success() {
        String deckId = "abc123";

        mockServer.expect(requestTo("/abc123/draw/?count=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "success":true,
                          "deck_id":"%s",
                          "cards":[
                            {"code":"AS","value":"ACE","suit":"SPADES","image":"https://deckofcardsapi.com/static/img/AS.png"},
                            {"code":"KH","value":"KING","suit":"HEARTS","image":"https://deckofcardsapi.com/static/img/KH.png"}
                          ],
                          "remaining":50
                        }
                        """.formatted(deckId), MediaType.APPLICATION_JSON));

        List<Card> result = client.draw(deckId, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("AS");
        assertThat(result.get(0).value()).isEqualTo("ACE");
        assertThat(result.get(0).suit()).isEqualTo("SPADES");
        assertThat(result.get(0).image()).isEqualTo("https://deckofcardsapi.com/static/img/AS.png");
        assertThat(result.get(1).code()).isEqualTo("KH");
        assertThat(result.get(1).value()).isEqualTo("KING");
        assertThat(result.get(1).suit()).isEqualTo("HEARTS");
        assertThat(result.get(1).image()).isEqualTo("https://deckofcardsapi.com/static/img/KH.png");
        mockServer.verify();
    }

    @Test
    @DisplayName("draw: 200 com success=false -> lança DeckApiException")
    void draw_successFalse() {
        String deckId = "abc123";

        mockServer.expect(requestTo("/abc123/draw/?count=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"success":false,"deck_id":"%s","cards":[],"remaining":0}
                        """.formatted(deckId), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.draw(deckId, 2))
                .isInstanceOf(DeckApiException.class)
                .hasMessage("Fail to draw deck");
        mockServer.verify();
    }

    @Test
    @DisplayName("draw: 500 erro de rede -> lança DeckApiException")
    void draw_serverError() {
        String deckId = "abc123";

        mockServer.expect(requestTo("/abc123/draw/?count=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.draw(deckId, 2))
                .isInstanceOf(DeckApiException.class)
                .hasMessage("Fail to draw deck");
        mockServer.verify();
    }
}