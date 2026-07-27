package com.wil.reservation_api.dto.event;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, String name, Instant startsAt) {
}
