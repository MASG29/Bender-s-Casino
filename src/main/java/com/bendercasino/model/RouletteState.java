package com.bendercasino.model;

public record RouletteState(int number, Colour colour, Colour betColour, boolean won, int payout) {}
