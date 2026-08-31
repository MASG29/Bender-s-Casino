package com.bendercasino.model.slots;

import java.util.List;

public record SlotsState(List<Symbol> symbols, Outcome outcome, double multiplier, int payout) {}
