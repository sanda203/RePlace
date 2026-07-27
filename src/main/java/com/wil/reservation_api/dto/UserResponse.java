package com.wil.reservation_api.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email) {
}
