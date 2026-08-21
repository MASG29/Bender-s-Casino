package com.bendercasino.exception;

public class DeckApiException extends RuntimeException {
    public DeckApiException(String message) {
        super(message);
    }
    public DeckApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
