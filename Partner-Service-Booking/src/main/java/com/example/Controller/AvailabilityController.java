package com.example.Controller;

import com.example.dto.Response.AvailabilitySearchResponse;
import com.example.dto.Request.RoomAvailabilityRequest;
import com.example.dto.Request.SlotAvailabilityRequest;
import com.example.dto.Request.TicketAvailabilityRequest;
import com.example.dto.base.ApiResponse;
import com.example.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/rooms/search")
    public ResponseEntity<ApiResponse<AvailabilitySearchResponse>> searchRooms(
            @Valid @RequestBody RoomAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(availabilityService.searchRooms(request)));
    }

    @PostMapping("/slots/search")
    public ResponseEntity<ApiResponse<AvailabilitySearchResponse>> searchSlots(
            @Valid @RequestBody SlotAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(availabilityService.searchSlots(request)));
    }

    @PostMapping("/tickets/search")
    public ResponseEntity<ApiResponse<AvailabilitySearchResponse>> searchTickets(
            @Valid @RequestBody TicketAvailabilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(availabilityService.searchTickets(request)));
    }
}
