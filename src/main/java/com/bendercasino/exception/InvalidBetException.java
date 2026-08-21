package com.bendercasino.exception;

public class InvalidBetException extends RuntimeException {
    public InvalidBetException(int bet) {
        super("Invalid bet: " + bet + ". The bet most be above 0 .");
    }
}
