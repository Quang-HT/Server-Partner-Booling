package com.example.dto.Request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ConfirmBookingRequest(
        @NotBlank @JsonAlias("holdToken") String holdId,
        @NotBlank String customerRef,
        @NotBlank String idempotencyKey
) {
}
