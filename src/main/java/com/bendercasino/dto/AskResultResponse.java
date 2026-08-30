package com.bendercasino.dto;

import java.util.List;

public record AskResultResponse(
        boolean gotCards,
        List<CardDto> cardsReceived,
        boolean drewFromDeck,
        CardDto drawnCard,
        boolean formedBook,
        String message,
        PeixinhoStateResponse gameState,
        BotAskDto botAsk
) {}
