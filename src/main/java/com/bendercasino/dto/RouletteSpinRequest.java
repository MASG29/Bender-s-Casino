package com.bendercasino.dto;

import com.bendercasino.model.Colour;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record RouletteSpinRequest(
        @NotNull UUID playerId,
        @Positive int bet,
        @NotNull Colour colour) {}
