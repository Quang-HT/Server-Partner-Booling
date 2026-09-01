package com.example.Controller;

import com.example.Service.BookingHoldService;
import com.example.dto.Request.BookingHoldRequest;
import com.example.dto.Response.BookingHoldResponse;
import com.example.dto.Response.HoldStatusResponse;
import com.example.dto.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class BookingHoldController {
    private final BookingHoldService bookingHoldService;

    @PostMapping("/holds")
    public ApiResponse<BookingHoldResponse> holdBooking(
            @Valid @RequestBody BookingHoldRequest bookingHoldRequest
    ) {
        return ApiResponse.success(bookingHoldService.holdBooking(bookingHoldRequest));
    }

    @GetMapping("/holds/{holdId}")
    public ApiResponse<HoldStatusResponse> getHoldStatus(@PathVariable String holdId) {
        return ApiResponse.success(bookingHoldService.getStatusHold(holdId));
    }

    @DeleteMapping("/holds/{holdId}")
    public ApiResponse<HoldStatusResponse> releaseHold(@PathVariable String holdId) {
        return ApiResponse.success(bookingHoldService.releaseHold(holdId));
    }
}
