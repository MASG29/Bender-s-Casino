package com.bendercasino.dto.roleta;

import com.bendercasino.model.roleta.BetType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record RouletteSpinRequest(
        @NotNull UUID playerId,
        @Positive int bet,
        @NotNull BetType betType) {}
