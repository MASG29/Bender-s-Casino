package com.bendercasino.dto;

import jakarta.validation.constraints.NotNull;

public record ActRequest(@NotNull String action, Object payload) {}
