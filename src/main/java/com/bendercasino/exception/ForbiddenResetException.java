package com.bendercasino.exception;

public class ForbiddenResetException extends RuntimeException {

    public ForbiddenResetException() {
        super("You can only reset your own account");
    }
}
