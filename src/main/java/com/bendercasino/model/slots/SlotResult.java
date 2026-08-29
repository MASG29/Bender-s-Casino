package com.bendercasino.model.slots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SlotResult {

    private List<Symbol> symbol;
    private Outcome outcome;

    public SlotResult(Symbol[] symbol) {
        this.symbol = new ArrayList<>();
        this.symbol.addAll(Arrays.asList(symbol));
    }

    public SymbolFrequency getFrequency() {

        int freq1 = Collections.frequency(symbol, symbol.get(0));
        int freq2 = Collections.frequency(symbol, symbol.get(1));

        if (freq1 == 3) {
            return new SymbolFrequency(3, symbol.get(0));
        }
        else if (freq1 == 2) {
            return new SymbolFrequency(2 , symbol.get(0));
        }
        else if (freq2 == 2) {
            return new SymbolFrequency(2 , symbol.get(1));
        }
        return new SymbolFrequency(1 , Symbol.NONE);

    }

    public List<Symbol> getSymbols() {
        return symbol;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Double getMultiplier() {
        SymbolFrequency sf = getFrequency();
        outcome = sf.getOutcome();
        Double mult = sf.getOutcome().getMultiplier();
        Double sym = sf.getSymbol().getValue();


        return mult * sym;
    }


}
