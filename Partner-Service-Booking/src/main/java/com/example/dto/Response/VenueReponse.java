package com.example.dto.Response;

import com.example.Enum.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VenueReponse {

    private String venueId;
    private String providerId;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private ServiceType serviceType;
    private Boolean isActive;
}