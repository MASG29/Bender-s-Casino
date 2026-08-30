package com.bendercasino.model.slots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Slot {

    private List<Symbol> multipliers;


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
