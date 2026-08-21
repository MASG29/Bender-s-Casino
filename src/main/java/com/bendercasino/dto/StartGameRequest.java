package com.bendercasino.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record StartGameRequest(@NotNull UUID playerId, @Positive int bet) {}
