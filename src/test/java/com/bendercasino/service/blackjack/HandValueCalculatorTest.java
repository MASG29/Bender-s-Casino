package com.bendercasino.service.blackjack;

import com.bendercasino.model.Card;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HandValueCalculatorTest {

    private static Card card(String value, String suit) {
        return new Card(value.substring(0, 1) + suit.substring(0, 1), value, suit, "");
    }

    @Test @DisplayName("mao vazia = 0")
    void emptyHand() {
        assertThat(HandValueCalculator.value(List.of())).isEqualTo(0);
    }

    @Test @DisplayName("2+3 = 5")
    void twoAndThree() {
        assertThat(HandValueCalculator.value(List.of(
                card("2", "SPADES"), card("3", "HEARTS")))).isEqualTo(5);
    }

    @Test @DisplayName("K+Q = 20")
    void kingAndQueen() {
        assertThat(HandValueCalculator.value(List.of(
                card("KING", "DIAMONDS"), card("QUEEN", "CLUBS")))).isEqualTo(20);
    }

    @Test @DisplayName("A+K = 21, isBlackjack")
    void aceAndKingIsBlackjack() {
        var cards = List.of(card("ACE", "SPADES"), card("KING", "HEARTS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(21);
    }

    @Test @DisplayName("A+A+9 = 21 (classico)")
    void twoAcesAndNine() {
        assertThat(HandValueCalculator.value(List.of(
                card("ACE", "SPADES"), card("ACE", "HEARTS"), card("9", "CLUBS")))).isEqualTo(21);
    }

    @Test @DisplayName("A+A+A+8 = 21")
    void threeAcesAndEight() {
        assertThat(HandValueCalculator.value(List.of(
                card("ACE", "SPADES"), card("ACE", "HEARTS"),
                card("ACE", "DIAMONDS"), card("8", "CLUBS")))).isEqualTo(21);
    }

    @Test @DisplayName("A+6 = 17, soft")
    void aceAndSixSoft() {
        var cards = List.of(card("ACE", "SPADES"), card("6", "HEARTS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(17);
        assertThat(HandValueCalculator.isSoft(cards)).isTrue();
    }

    @Test @DisplayName("A+6+K = 17, hard")
    void aceAndSixAndKingHard() {
        var cards = List.of(card("ACE", "SPADES"), card("6", "HEARTS"), card("KING", "DIAMONDS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(17);
        assertThat(HandValueCalculator.isSoft(cards)).isFalse();
    }

    @Test @DisplayName("A+A = 12, soft")
    void twoAcesSoft() {
        var cards = List.of(card("ACE", "SPADES"), card("ACE", "HEARTS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(12);
        assertThat(HandValueCalculator.isSoft(cards)).isTrue();
    }

    @Test @DisplayName("K+Q+2 = 22, busted")
    void busted() {
        var cards = List.of(card("KING", "SPADES"), card("QUEEN", "HEARTS"), card("2", "CLUBS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(22);
    }

    @Test @DisplayName("5+5+5+5+A = 21")
    void fourFivesAndAce() {
        assertThat(HandValueCalculator.value(List.of(
                card("5", "SPADES"), card("5", "HEARTS"),
                card("5", "DIAMONDS"), card("5", "CLUBS"),
                card("ACE", "SPADES")))).isEqualTo(21);
    }

    @Test @DisplayName("10+9+2 = 21, NAO e blackjack natural")
    void twentyOneInThreeCardsIsNotBlackjack() {
        var cards = List.of(card("10", "SPADES"), card("9", "HEARTS"), card("2", "CLUBS"));
        assertThat(HandValueCalculator.value(cards)).isEqualTo(21);
    }
}
