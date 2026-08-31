package com.bendercasino.dto.peixinho;

import com.bendercasino.dto.CardDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PeixinhoStateResponse(
        UUID gameId,
        UUID currentPlayerId,
        List<CardDto> playerHand,
        Map<UUID, Integer> opponentHandSizes,
        int deckSize,
        List<BookDto> books,
        String status,
        String lastAction,
        boolean canAskAgain
) {}
