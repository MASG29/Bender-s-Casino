package com.bendercasino.dto.videopoker;

import com.bendercasino.dto.CardDto;
import com.bendercasino.model.videopoker.PokerHandCategory;

import java.util.List;

public record DrawResponse(
        List<CardDto> cards,
        PokerHandCategory category,
        int payout,
        int balance
) {}
