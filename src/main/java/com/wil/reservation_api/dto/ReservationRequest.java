package com.wil.reservation_api.dto;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(UUID userId, UUID eventId, List<UUID> seatIds) {
}
