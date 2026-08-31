package com.bendercasino.dto.slots;

import com.bendercasino.model.slots.Outcome;
import com.bendercasino.model.slots.Symbol;

import java.util.List;

public record SlotsSpinResponse(
        List<Symbol> symbols,
        Outcome outcome,
        int payout,
        int balance) {}
