package com.wil.reservation_api.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EventRequest(@NotBlank String name, @NotNull Instant startsAt) {
}
