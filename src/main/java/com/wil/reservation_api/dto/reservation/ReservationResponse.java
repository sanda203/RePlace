package com.wil.reservation_api.dto.reservation;

import com.wil.reservation_api.entity.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse (UUID reservationId, ReservationStatus status, Instant expiresAt, List<UUID> seatIds){
}
