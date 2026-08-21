package com.bendercasino.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String playerName, int balance, int bet) {
        super(playerName + " have " + balance + " coins, impossible to bet " + bet + ".");
    }
}
