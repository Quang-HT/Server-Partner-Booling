package com.example.dto.Response;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingInventoryResponse(
        String inventoryId,
        LocalDate serviceDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer quantity
) {
}
