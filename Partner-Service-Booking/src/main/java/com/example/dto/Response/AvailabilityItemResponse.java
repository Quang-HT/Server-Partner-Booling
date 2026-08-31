package com.example.dto.Response;

import com.example.Enum.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityItemResponse(
        String providerId,
        String venueId,
        String venueName,
        String optionId,
        String optionName,
        ServiceType serviceType,
        Integer capacity,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalDate serviceDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer availableQuantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String currency
) {
}
