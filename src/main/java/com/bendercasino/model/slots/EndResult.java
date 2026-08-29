package com.bendercasino.model.slots;

import java.util.ArrayList;
import java.util.List;

public class EndResult {

    private Outcome outcome;
    private List<Symbol> symbols;
    private int payout;
    private Double multiplier;

    public EndResult(Double multiplier, int payout, List<Symbol> symbols, Outcome outcome) {
        this.multiplier = multiplier;
        this.payout = payout;
        this.symbols = symbols;
        this.outcome = outcome;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbol) {
        this.symbols = symbol;
    }

    public int getPayout() {
        return payout;
    }

    public void setPayout(int payout) {
        this.payout = payout;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }
}
