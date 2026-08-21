package com.bendercasino.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlayerActionRequest(@NotNull UUID playerId) {}
