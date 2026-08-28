package com.bendercasino.model.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SlotResult {

    private Outcome outcome;
    private List<Symbol> symbol;

    public SlotResult(Outcome outcome, Symbol[] symbol) {
        this.outcome = outcome;
        this.symbol = new ArrayList<>();
        this.symbol.addAll(Arrays.asList(symbol));
    }

    public Symbol getSymbol() {
        Symbol matchedSymb;
        int freq1 = Collections.frequency(symbol, symbol.get(0));
        int freq2 = Collections.frequency(symbol, symbol.get(1));

        if (freq1 == 3) {
            return symbol.get(0);
        }
        else if (freq1 == 2 || freq2 == 2) {
            if (freq1 > freq2) {
               return symbol.get(0);
            }
            else {
                return symbol.get(1);
            }
        }
        return Symbol.NONE;
    }

    public Double getMultiplier() {
        Double multiplier = getSymbol().getValue();
        return multiplier * outcome.getMultiplier();
    }
}
