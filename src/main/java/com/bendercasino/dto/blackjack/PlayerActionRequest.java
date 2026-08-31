package com.bendercasino.dto.blackjack;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlayerActionRequest(@NotNull UUID playerId) {}
