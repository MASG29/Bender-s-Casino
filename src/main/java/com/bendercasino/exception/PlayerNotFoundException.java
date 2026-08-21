package com.bendercasino.exception;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(UUID id) {
        super("Player not found: " + id);
    }
}
