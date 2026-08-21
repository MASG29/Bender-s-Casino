package com.bendercasino.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(@NotBlank String name) {}
