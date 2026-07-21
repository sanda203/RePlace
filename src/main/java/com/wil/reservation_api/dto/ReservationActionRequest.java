package com.wil.reservation_api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReservationActionRequest(@NotNull UUID userId) {
}
