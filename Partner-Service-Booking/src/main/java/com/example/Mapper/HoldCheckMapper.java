package com.example.Mapper;

import com.example.Model.BookingHold;
import com.example.dto.Response.HoldStatusResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HoldCheckMapper {
    public HoldStatusResponse toResponse(
            BookingHold bookingHold,
            BigDecimal totalAmount,
            Integer remainingSeconds
    ) {
        return new HoldStatusResponse(
                bookingHold.getHoldId(),
                bookingHold.getStatus(),
                bookingHold.getQuantity(),
                totalAmount,
                bookingHold.getExpiresAt(),
                remainingSeconds
        );
    }
}
