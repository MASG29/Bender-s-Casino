package com.bendercasino.model.slots;

public class SymbolFrequency {
    private int count;
    private Symbol symbol;

    public SymbolFrequency(int count, Symbol symbol) {
        this.count = count;
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Outcome getOutcome() {
        return switch (count) {
            case 3 -> Outcome.WIN;
            case 2 -> Outcome.CONSOLATION;
            default -> Outcome.LOSS;
        };
    }
}
