package com.wil.reservation_api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(@NotNull UUID userId, @NotNull UUID eventId, @NotEmpty List<@NotNull UUID> seatIds) {
}
