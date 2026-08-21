package com.bendercasino.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(UUID playerId) {
        super("Without games for player: " + playerId);
    }
}
