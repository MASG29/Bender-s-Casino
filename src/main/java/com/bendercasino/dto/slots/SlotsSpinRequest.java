package com.bendercasino.dto.slots;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record SlotsSpinRequest(
        @NotNull UUID playerId,
        @Positive int betAmount) {}
