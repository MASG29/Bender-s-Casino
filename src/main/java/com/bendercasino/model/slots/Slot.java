package com.bendercasino.model.slots;

import java.util.ArrayList;
import java.util.List;

public class Slot {

    private final List<Symbol> multipliers;

    public Slot() {
        multipliers = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) {
            if (symbol != Symbol.NONE) {
                multipliers.add(symbol);
            }
        }
    }

    public List<Symbol> getMultipliers() {
        return multipliers;
    }
}
