package com.bendercasino.dto;

import java.util.List;
import java.util.UUID;

public record DealResponse(UUID handId, List<CardDto> cards) {}
