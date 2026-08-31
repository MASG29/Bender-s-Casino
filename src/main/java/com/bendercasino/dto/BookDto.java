package com.bendercasino.dto;

import java.util.UUID;

public record BookDto(
        UUID playerId,
        String cardValue
) {}
