package com.bendercasino.dto.blackjack;

import java.util.UUID;

public record GameStateResponse(
        UUID gameId,
        UUID playerId,
        String status,
        HandDto playerHand,
        HandDto dealerHand,
        int bet,
        String outcome,
        int payout,
        String benderJoke,
        StreaksDto streaks
) {
    public record StreaksDto(int wins, int losses, int blackjacks) {}
}
