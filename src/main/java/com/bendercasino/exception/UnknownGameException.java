package com.bendercasino.exception;

public class UnknownGameException extends RuntimeException {
    public UnknownGameException(String game) {
        super("Unknown game: " + game);
    }
}
