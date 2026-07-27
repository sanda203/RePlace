package com.wil.reservation_api.dto.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSeatsRequest(@NotEmpty List<@NotBlank String> labels) {
}
