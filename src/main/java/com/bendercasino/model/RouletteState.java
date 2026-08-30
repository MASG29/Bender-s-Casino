package com.bendercasino.model;

public record RouletteState(int number, Colour colour, BetType betType, boolean won, int payout) {}
