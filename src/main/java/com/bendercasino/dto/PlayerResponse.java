package com.bendercasino.dto;

import java.util.UUID;

public record PlayerResponse(UUID playerId, String name, int balance, StatsDto stats) {
    public record StatsDto(int wins, int losses, int pushes, int blackjacks) {}
}
