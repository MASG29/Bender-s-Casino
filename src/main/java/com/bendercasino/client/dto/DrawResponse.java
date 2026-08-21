package com.bendercasino.client.dto;

import java.util.List;

public record DrawResponse(boolean success, String deck_id, List<ApiCard> cards, int remaining) {}
