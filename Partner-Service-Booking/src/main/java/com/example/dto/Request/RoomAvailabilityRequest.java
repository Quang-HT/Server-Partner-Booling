package com.example.dto.Request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomAvailabilityRequest(
        String venueId,
        @NotNull @FutureOrPresent LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive Integer guestCount,
        @PositiveOrZero BigDecimal maxPrice
) {
}
