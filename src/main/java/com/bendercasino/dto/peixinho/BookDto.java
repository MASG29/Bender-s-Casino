package com.bendercasino.dto.peixinho;

import java.util.UUID;

public record BookDto(
        UUID playerId,
        String cardValue
) {}
