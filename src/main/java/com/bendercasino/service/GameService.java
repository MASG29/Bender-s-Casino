package com.bendercasino.service;

import com.bendercasino.model.GameSession;

import java.util.UUID;

public interface GameService {
    GameSession start(UUID playerId, int bet);
    GameSession act(UUID gameId, String action, Object payload);
    GameSession state(UUID gameId);
}
