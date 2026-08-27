package com.bendercasino.dto;

import com.bendercasino.model.Colour;

public record RouletteSpinResponse(
        int number,
        Colour colour,
        boolean won,
        int payout,
        int balance) {}
