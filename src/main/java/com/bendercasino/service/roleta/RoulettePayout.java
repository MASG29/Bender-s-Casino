package com.bendercasino.service.roleta;

import com.bendercasino.model.roleta.BetType;
import com.bendercasino.model.roleta.Colour;


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

    public static int payout(int bet, BetType betType, int spunNumber) {
        return wins(betType, spunNumber) ? bet * 2 : 0;
    }

    private static boolean wins(BetType betType, int spunNumber) {
        if (spunNumber == 0) {
            return false;
        }
        return switch (betType) {
            case RED -> colourOf(spunNumber) == Colour.RED;
            case BLACK -> colourOf(spunNumber) == Colour.BLACK;
            case ODD -> spunNumber % 2 != 0;
            case EVEN -> spunNumber % 2 == 0;
            case LOW -> spunNumber >= 1 && spunNumber <= 18;
            case HIGH -> spunNumber >= 19 && spunNumber <= 36;
        };
    }
}
