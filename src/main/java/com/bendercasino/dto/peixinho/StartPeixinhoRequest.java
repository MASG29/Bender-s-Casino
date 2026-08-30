package com.bendercasino.dto.peixinho;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;


public record StartPeixinhoRequest(
        @NotNull UUID playerId,
        @Positive int bet
) {}
