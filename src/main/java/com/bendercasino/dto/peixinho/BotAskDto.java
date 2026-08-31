package com.bendercasino.dto.peixinho;

import java.util.UUID;

public record BotAskDto(
        UUID askerId,
        String cardValue,
        boolean gotCards,
        int cardsReceivedCount,
        boolean fished,
        boolean caughtAskedCard,
        boolean formedBook
) {}
