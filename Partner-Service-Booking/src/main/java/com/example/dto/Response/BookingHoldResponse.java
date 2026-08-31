package com.example.dto.Response;

import com.example.Enum.BookingHoldStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingHoldResponse {
    private String holdId;
    private String optionId;
    private BookingHoldStatus status;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime expiresAt;
}
