package com.bendercasino.dto.peixinho;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AskCardRequest(
        @NotNull UUID playerId,
        @NotNull UUID targetId,
        @NotBlank String cardValue
) {}
