package com.example.dto.Request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record SlotAvailabilityRequest(
        String venueId,
        @NotNull @FutureOrPresent LocalDate serviceDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive Integer guestCount,
        @PositiveOrZero BigDecimal maxPrice
) {
}
