package com.bendercasino.model.roleta;

public record RouletteState(int number, Colour colour, BetType betType, boolean won, int payout) {}
