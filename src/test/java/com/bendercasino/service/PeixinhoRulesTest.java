package com.bendercasino.service;

import com.bendercasino.dto.BookDto;
import com.bendercasino.model.Card;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PeixinhoRulesTest {

    private static Card card(String value, String suit) {
        return new Card(value + suit.substring(0, 1), value, suit, "");
    }

    @Test
    void canAsk_onlyWhenValueIsInHand() {
        var hand = List.of(card("7", "HEARTS"));

        assertThat(PeixinhoRules.canAsk(hand, "7")).isTrue();
        assertThat(PeixinhoRules.canAsk(hand, "KING")).isFalse();
    }

    @Test
    void cardsOfValue_returnsOnlyRequestedCards() {
        var hand = List.of(card("7", "HEARTS"), card("KING", "SPADES"), card("7", "CLUBS"));

        assertThat(PeixinhoRules.cardsOfValue(hand, "7")).containsExactly(hand.get(0), hand.get(2));
        assertThat(PeixinhoRules.cardsOfValue(hand, "ACE")).isEmpty();
    }

    @Test
    void isBook_requiresExactlyFourCards() {
        assertThat(PeixinhoRules.isBook(List.of(
                card("7", "HEARTS"), card("7", "DIAMONDS"), card("7", "CLUBS"), card("7", "SPADES")), "7"))
                .isTrue();
        assertThat(PeixinhoRules.isBook(List.of(
                card("7", "HEARTS"), card("7", "DIAMONDS"), card("7", "CLUBS")), "7"))
                .isFalse();
    }

    @Test
    void isGameOver_requiresAtLeastThirteenBooks() {
        assertThat(PeixinhoRules.isGameOver(12)).isFalse();
        assertThat(PeixinhoRules.isGameOver(13)).isTrue();
        assertThat(PeixinhoRules.isGameOver(14)).isTrue();
    }

    @Test
    void countBooks_countsOnlyBooksOfPlayer() {
        UUID player = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        var books = List.of(
                new BookDto(player, "2"), new BookDto(player, "3"), new BookDto(other, "4"));

        assertThat(PeixinhoRules.countBooks(books, player)).isEqualTo(2);
        assertThat(PeixinhoRules.countBooks(books, other)).isEqualTo(1);
    }

    @Test
    void winner_returnsPlayerWithMostBooks() {
        UUID player = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertThat(PeixinhoRules.winner(Map.of(player, 5, other, 3))).isEqualTo(player);
        assertThat(PeixinhoRules.winner(Map.of(player, 13))).isEqualTo(player);
    }
}
