package com.example.dto.Response;

import com.example.Enum.ServiceType;

import java.util.List;

public record AvailabilitySearchResponse(
        ServiceType serviceType,
        List<AvailabilityItemResponse> options
) {
}
