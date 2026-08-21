package com.bendercasino.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JokeRequest(@NotNull UUID playerId, @NotNull String trigger) {}
