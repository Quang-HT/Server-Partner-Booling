package com.example.Controller;

import com.example.Service.BookingOptionService;
import com.example.dto.Response.BookingOptionResponse;
import com.example.dto.base.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class BookingOptionController {
    private final BookingOptionService bookingOptionService;

    @GetMapping("/options/{optionId}")
    public ApiResponse<BookingOptionResponse> getDetailBookingOption(
            @PathVariable String optionId
    ){
        return ApiResponse.success(bookingOptionService.getDetailBookingOption(optionId));
    }
}
