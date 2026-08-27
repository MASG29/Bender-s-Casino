package com.bendercasino.service;

import com.bendercasino.model.Colour;

import java.util.Set;

public class RoulettePayout {

    private static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    private RoulettePayout() {}

    public static Colour colourOf(int number) {
        if (number < 0 || number > 36) {
            throw new IllegalArgumentException("Number must be between 0 and 36, got " + number);
        }
        if (number == 0) {
            return Colour.GREEN;
        }
        return RED_NUMBERS.contains(number) ? Colour.RED : Colour.BLACK;
    }

    public static int payout(int bet, Colour betColour, int spunNumber) {
        if (betColour == Colour.GREEN) {
            throw new IllegalArgumentException("Cannot bet on green");
        }
        return colourOf(spunNumber) == betColour ? bet * 2 : 0;
    }
}
