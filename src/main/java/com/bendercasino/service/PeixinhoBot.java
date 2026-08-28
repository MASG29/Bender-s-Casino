package com.bendercasino.service;

import com.bendercasino.dto.BookDto;
import com.bendercasino.model.Card;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PeixinhoBot {

    private final Random random = new Random();

    public BotDecision decide(
            List<Card> botHand,
            List<UUID> opponentIds,
            List<BookDto> books,
            List<AskHistoryEntry> askHistory) {

        if (botHand.isEmpty() || opponentIds.isEmpty()) {
            throw new IllegalStateException("Bot without cards or opponents");
        }

        Set<String> exhausted = new HashSet<>();
        books.forEach(b -> exhausted.add(b.cardValue()));


        List<String> askableValues = botHand.stream()
                .map(Card::value)
                .distinct()
                .filter(v -> !exhausted.contains(v))
                .toList();


        for (AskHistoryEntry entry : askHistory) {
            if (askableValues.contains(entry.cardValue())
                    && opponentIds.contains(entry.requesterId())) {
                return new BotDecision(entry.requesterId(), entry.cardValue());
            }
        }


        String chosenValue    = askableValues.get(random.nextInt(askableValues.size()));
        UUID   chosenOpponent = opponentIds.get(random.nextInt(opponentIds.size()));

        return new BotDecision(chosenOpponent, chosenValue);
    }

    public record BotDecision(UUID targetId, String cardValue) {}

    public record AskHistoryEntry(UUID requesterId, UUID targetId, String cardValue) {}
}