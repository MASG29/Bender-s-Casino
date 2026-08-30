package com.bendercasino.dto.roleta;

import com.bendercasino.model.roleta.Colour;


public record RouletteSpinResponse(
        int number,
        Colour colour,
        boolean won,
        int payout,
        int balance) {}
