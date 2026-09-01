package com.example.dto.Response;

import com.example.Enum.BookingHoldStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class HoldStatusResponse {
    private String holdId;
    private BookingHoldStatus status;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private Integer remainingSeconds;
}
