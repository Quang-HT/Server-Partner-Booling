package com.example.dto.Request;

import com.example.Enum.BookingHoldStatus;
import com.example.Enum.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@Data
public class BookingHoldRequest {

    private String optionId;
    private Integer quantity;

    private LocalDate serviceDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Integer guestCount;
}
