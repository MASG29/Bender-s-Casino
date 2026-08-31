package com.bendercasino.dto.videopoker;

import com.bendercasino.dto.CardDto;

import java.util.List;
import java.util.UUID;

public record DealResponse(UUID handId, List<CardDto> cards) {}
