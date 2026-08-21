package com.bendercasino.dto;

import java.util.List;

public record HandDto(List<CardDto> cards, int value, boolean soft, boolean blackjack, boolean busted) {}
