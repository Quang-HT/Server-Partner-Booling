package com.example.Controller;

import com.example.Enum.ServiceType;
import com.example.Service.VenueService;
import com.example.dto.Response.BookingOptionResponse;
import com.example.dto.Response.VenueReponse;
import com.example.dto.base.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/venues")
    public ApiResponse<List<VenueReponse>> getVenues(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ServiceType serviceType
    ){
        List<VenueReponse> list = venueService.searchVenue(keyword, serviceType);

        return ApiResponse.success(list);
    }

    @GetMapping("/venues/{venueId}")
    public ApiResponse<VenueReponse> getDetailVenue(
            @PathVariable String venueId
    ){
        return ApiResponse.success(venueService.detailVenue(venueId));
    }

    @GetMapping("/venues/{venueId}/options")
    public ApiResponse<List<BookingOptionResponse>> getAllBookingOptions(
            @PathVariable String venueId
    ){
        return ApiResponse.success(venueService.findAllBookingOptionByVenueId(venueId));
    }
}
