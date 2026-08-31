package com.example.Mapper;

import com.example.Model.Venue;
import com.example.dto.Response.VenueReponse;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {
    public VenueReponse toResponse(Venue venue){
        return new VenueReponse(
                venue.getVenueId(),
                venue.getProviderId(),
                venue.getName(),
                venue.getDescription(),
                venue.getAddress(),
                venue.getLatitude(),
                venue.getLongitude(),
                venue.getServiceType(),
                venue.getIsActive()
        );
    }
}
