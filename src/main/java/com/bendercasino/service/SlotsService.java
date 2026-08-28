package com.bendercasino.service;

import com.bendercasino.model.slots.Outcome;
import com.bendercasino.model.slots.Slot;
import com.bendercasino.model.slots.SlotResult;
import com.bendercasino.model.slots.Symbol;
import org.springframework.stereotype.Service;

@Service
public class SlotsService {

    private Slot slot;
    private Integer betAmount;

    public SlotsService() {
        this.slot = new Slot();
    }

    public Double bet(int amount) {
        SlotResult roll = roll();
        return amount * roll.getMultiplier();
    }

    public SlotResult roll() {

        Symbol[] symb = new Symbol[3];

        for (int i = 0; i < symb.length; i++) {
            symb[i] = slot.getMultipliers().get((int) Math.floor(Math.random() * (slot.getMultipliers().size())));
        }

        if (symb[0] == symb[1] && symb[0] == symb[2]) {
            return new SlotResult(Outcome.WIN, symb);
        } else if (symb[0] == symb[1] && symb[1] != symb[2] || symb[1] == symb[2] && symb[0] != symb[1]) {
            return new SlotResult(Outcome.CONSOLATION, symb);
        }
        return new SlotResult(Outcome.LOSS, symb);
    }


}
