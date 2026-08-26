package com.bendercasino.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlayerRequest(
    @NotBlank String name,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String email,
    @NotBlank @Size(min = 6) String password
) {}