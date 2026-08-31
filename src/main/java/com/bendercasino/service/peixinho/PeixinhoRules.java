package com.bendercasino.service.peixinho;

import com.bendercasino.dto.peixinho.BookDto;
import com.bendercasino.model.Card;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PeixinhoRules {

    private PeixinhoRules() {}

    public static boolean canAsk(List<Card> hand, String cardValue) {
        return hand.stream().anyMatch(c -> c.value().equals(cardValue));
    }

    public static List<Card> cardsOfValue(List<Card> hand, String cardValue) {
        return hand.stream()
                .filter(c -> c.value().equals(cardValue))
                .toList();
    }

    public static boolean isBook(List<Card> hand, String cardValue) {
        return hand.stream()
                .filter(c -> c.value().equals(cardValue))
                .count() == 4;
    }

    public static boolean isGameOver(int totalBooks) {
        return totalBooks >= 13;
    }

    public static int countBooks(List<BookDto> books, UUID playerId) {
        return (int) books.stream()
                .filter(b -> b.playerId().equals(playerId))
                .count();
    }

    public static UUID winner(Map<UUID, Integer> bookCounts) {
        return bookCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}
