package com.example.dto.Response;

import com.example.Enum.PartnerBookingStatus;
import com.example.Enum.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PartnerBookingResponse(
        String providerBookingId,
        String holdId,
        String providerId,
        String venueId,
        String optionId,
        ServiceType serviceType,
        String customerRef,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String currency,
        PartnerBookingStatus status,
        List<BookingInventoryResponse> inventories,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
}
