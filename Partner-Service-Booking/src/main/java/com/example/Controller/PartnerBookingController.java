package com.example.Controller;

import com.example.Service.PartnerBookingService;
import com.example.dto.Request.ConfirmBookingRequest;
import com.example.dto.Response.PartnerBookingResponse;
import com.example.dto.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class PartnerBookingController {

    private final PartnerBookingService partnerBookingService;

    @PostMapping
    public ApiResponse<PartnerBookingResponse> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request
    ) {
        return ApiResponse.success(partnerBookingService.confirmBooking(request));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<PartnerBookingResponse> getBooking(@PathVariable String bookingId) {
        return ApiResponse.success(partnerBookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<PartnerBookingResponse> cancelBooking(@PathVariable String bookingId) {
        return ApiResponse.success(partnerBookingService.cancelBooking(bookingId));
    }
}
