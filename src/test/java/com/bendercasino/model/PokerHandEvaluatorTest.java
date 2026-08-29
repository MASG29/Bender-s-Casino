package com.bendercasino.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokerHandEvaluatorTest {

    private static Card card(String value, String suit) {
        return new Card(value.substring(0, 1) + suit.substring(0, 1), value, suit, "");
    }

    private static PokerHandCategory evaluate(Card... cards) {
        return PokerHandEvaluator.evaluate(List.of(cards));
    }

    @Test @DisplayName("high card is nothing")
    void highCard() {
        assertThat(evaluate(
                card("2", "HEARTS"), card("5", "SPADES"), card("8", "CLUBS"),
                card("9", "DIAMONDS"), card("KING", "HEARTS")))
                .isEqualTo(PokerHandCategory.NOTHING);
    }

    @Test @DisplayName("pair of tens is nothing")
    void pairOfTensIsNothing() {
        assertThat(evaluate(
                card("10", "HEARTS"), card("10", "SPADES"), card("3", "CLUBS"),
                card("7", "DIAMONDS"), card("KING", "HEARTS")))
                .isEqualTo(PokerHandCategory.NOTHING);
    }

    @Test @DisplayName("pair of jacks")
    void pairOfJacks() {
        assertThat(evaluate(
                card("JACK", "HEARTS"), card("JACK", "SPADES"), card("3", "CLUBS"),
                card("7", "DIAMONDS"), card("KING", "HEARTS")))
                .isEqualTo(PokerHandCategory.JACKS_OR_BETTER);
    }

    @Test @DisplayName("pair of aces")
    void pairOfAces() {
        assertThat(evaluate(
                card("ACE", "HEARTS"), card("ACE", "SPADES"), card("4", "CLUBS"),
                card("8", "DIAMONDS"), card("9", "HEARTS")))
                .isEqualTo(PokerHandCategory.JACKS_OR_BETTER);
    }

    @Test @DisplayName("two pair")
    void twoPair() {
        assertThat(evaluate(
                card("5", "HEARTS"), card("5", "SPADES"), card("9", "CLUBS"),
                card("9", "DIAMONDS"), card("2", "HEARTS")))
                .isEqualTo(PokerHandCategory.TWO_PAIR);
    }

    @Test @DisplayName("three of a kind")
    void threeOfAKind() {
        assertThat(evaluate(
                card("8", "HEARTS"), card("8", "SPADES"), card("8", "CLUBS"),
                card("3", "DIAMONDS"), card("KING", "HEARTS")))
                .isEqualTo(PokerHandCategory.THREE_OF_A_KIND);
    }

    @Test @DisplayName("straight 5-9")
    void straight() {
        assertThat(evaluate(
                card("5", "HEARTS"), card("6", "SPADES"), card("7", "CLUBS"),
                card("8", "DIAMONDS"), card("9", "HEARTS")))
                .isEqualTo(PokerHandCategory.STRAIGHT);
    }

    @Test @DisplayName("wheel straight A-2-3-4-5")
    void wheelStraight() {
        assertThat(evaluate(
                card("ACE", "HEARTS"), card("2", "SPADES"), card("3", "CLUBS"),
                card("4", "DIAMONDS"), card("5", "HEARTS")))
                .isEqualTo(PokerHandCategory.STRAIGHT);
    }

    @Test @DisplayName("broadway straight 10-J-Q-K-A")
    void broadwayStraight() {
        assertThat(evaluate(
                card("10", "HEARTS"), card("JACK", "SPADES"), card("QUEEN", "CLUBS"),
                card("KING", "DIAMONDS"), card("ACE", "HEARTS")))
                .isEqualTo(PokerHandCategory.STRAIGHT);
    }

    @Test @DisplayName("A-2-3-4-6 is not a straight")
    void aceDoesNotWrap() {
        assertThat(evaluate(
                card("ACE", "HEARTS"), card("2", "SPADES"), card("3", "CLUBS"),
                card("4", "DIAMONDS"), card("6", "HEARTS")))
                .isEqualTo(PokerHandCategory.NOTHING);
    }

    @Test @DisplayName("flush")
    void flush() {
        assertThat(evaluate(
                card("2", "HEARTS"), card("6", "HEARTS"), card("9", "HEARTS"),
                card("JACK", "HEARTS"), card("KING", "HEARTS")))
                .isEqualTo(PokerHandCategory.FLUSH);
    }

    @Test @DisplayName("full house")
    void fullHouse() {
        assertThat(evaluate(
                card("7", "HEARTS"), card("7", "SPADES"), card("7", "CLUBS"),
                card("QUEEN", "DIAMONDS"), card("QUEEN", "HEARTS")))
                .isEqualTo(PokerHandCategory.FULL_HOUSE);
    }

    @Test @DisplayName("four of a kind")
    void fourOfAKind() {
        assertThat(evaluate(
                card("4", "HEARTS"), card("4", "SPADES"), card("4", "CLUBS"),
                card("4", "DIAMONDS"), card("ACE", "HEARTS")))
                .isEqualTo(PokerHandCategory.FOUR_OF_A_KIND);
    }

    @Test @DisplayName("straight flush")
    void straightFlush() {
        assertThat(evaluate(
                card("5", "SPADES"), card("6", "SPADES"), card("7", "SPADES"),
                card("8", "SPADES"), card("9", "SPADES")))
                .isEqualTo(PokerHandCategory.STRAIGHT_FLUSH);
    }

    @Test @DisplayName("wheel straight flush")
    void wheelStraightFlush() {
        assertThat(evaluate(
                card("ACE", "CLUBS"), card("2", "CLUBS"), card("3", "CLUBS"),
                card("4", "CLUBS"), card("5", "CLUBS")))
                .isEqualTo(PokerHandCategory.STRAIGHT_FLUSH);
    }

    @Test @DisplayName("royal flush")
    void royalFlush() {
        assertThat(evaluate(
                card("10", "HEARTS"), card("JACK", "HEARTS"), card("QUEEN", "HEARTS"),
                card("KING", "HEARTS"), card("ACE", "HEARTS")))
                .isEqualTo(PokerHandCategory.ROYAL_FLUSH);
    }
}
