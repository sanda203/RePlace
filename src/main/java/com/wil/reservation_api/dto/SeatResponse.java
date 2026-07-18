package com.wil.reservation_api.dto;

import com.wil.reservation_api.entity.SeatStatus;

import java.util.UUID;

public record SeatResponse(UUID id, String label, SeatStatus status) {
}
