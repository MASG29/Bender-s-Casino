package com.bendercasino.model.slots;

public enum Symbol {
    CHERRY(1.0),
    BAR(1.5),
    DOUBLE_BAR(2.0),
    TRIPLE_BAR(3.0),
    BELL(4.0),
    LEELA(5.0),
    FRY(6.0),
    BENDER(10.0),
    NONE(0.0)
    ;

    private final Double multiplier;

    Symbol(Double multiplier) {
        this.multiplier = multiplier;
    }

    public Double getValue() {
        return multiplier;
    }
}
