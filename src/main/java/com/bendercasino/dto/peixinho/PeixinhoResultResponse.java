package com.bendercasino.dto.peixinho;

import java.util.Map;
import java.util.UUID;

public record PeixinhoResultResponse(
        UUID winnerId,
        Map<UUID, Integer> bookCounts,
        Map<UUID, Integer> payouts,
        PeixinhoStateResponse finalState
) {}
