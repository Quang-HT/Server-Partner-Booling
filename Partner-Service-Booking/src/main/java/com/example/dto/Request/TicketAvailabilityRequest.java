package com.example.dto.Request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TicketAvailabilityRequest(
        String venueId,
        @NotNull @FutureOrPresent LocalDate serviceDate,
        LocalTime startTime,
        LocalTime endTime,
        @NotNull @Positive Integer quantity,
        @PositiveOrZero BigDecimal maxPrice
) {
}
