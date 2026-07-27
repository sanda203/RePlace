package com.wil.reservation_api.dto.auth;

import java.util.UUID;

public record UserResponse(UUID id, String email) {
}
