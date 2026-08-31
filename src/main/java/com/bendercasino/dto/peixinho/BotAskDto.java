package com.bendercasino.dto.peixinho;

import java.util.UUID;

public record BotAskDto(
        UUID askerId,
        String cardValue,
        boolean gotCards,
        boolean fished,
        boolean formedBook
) {}
