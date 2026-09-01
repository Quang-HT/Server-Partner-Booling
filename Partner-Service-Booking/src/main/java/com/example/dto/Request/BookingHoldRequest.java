package com.example.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookingHoldRequest {

    @NotBlank
    private String optionId;

    @NotNull
    @Positive
    private Integer quantity;

    private LocalDate serviceDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    @Positive
    private Integer guestCount;
}
