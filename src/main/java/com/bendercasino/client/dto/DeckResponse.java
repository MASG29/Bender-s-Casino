package com.bendercasino.client.dto;

public record DeckResponse(boolean success, String deck_id, int remaining, boolean shuffled) {}
