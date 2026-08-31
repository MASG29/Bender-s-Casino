package com.bendercasino.dto.blackjack;

import com.bendercasino.dto.CardDto;

import java.util.List;

public record HandDto(List<CardDto> cards, int value, boolean soft, boolean blackjack, boolean busted) {}
