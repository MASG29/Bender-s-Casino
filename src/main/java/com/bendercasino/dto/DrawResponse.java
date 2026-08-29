package com.bendercasino.dto;

import com.bendercasino.model.PokerHandCategory;

import java.util.List;

public record DrawResponse(
        List<CardDto> cards,
        PokerHandCategory category,
        int payout,
        int balance
) {}
