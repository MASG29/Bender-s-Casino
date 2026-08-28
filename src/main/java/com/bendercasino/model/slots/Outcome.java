package com.bendercasino.model.slots;

public enum Outcome {
    WIN(1.0),
    LOSS(0.0),
    CONSOLATION(0.5);

    private double multiplier;

    Outcome(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
