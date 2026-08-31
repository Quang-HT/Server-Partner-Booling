package com.example.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingOptionResponse {
    private String optionId;
    private String venueId;
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private Boolean isActive;
}
