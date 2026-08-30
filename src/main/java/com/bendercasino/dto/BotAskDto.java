package com.bendercasino.dto;

import java.util.UUID;

public record BotAskDto(
        UUID askerId,
        String cardValue,
        boolean gotCards,
        boolean fished,
        boolean formedBook
) {}
