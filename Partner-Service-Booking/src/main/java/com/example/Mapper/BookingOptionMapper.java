package com.example.Mapper;

import com.example.Model.BookingOption;
import com.example.dto.Response.BookingOptionResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingOptionMapper {
    public BookingOptionResponse toResponse(
            BookingOption bookingOption
    ){
        return new BookingOptionResponse(
                bookingOption.getOptionId(),
                bookingOption.getVenueId(),
                bookingOption.getName(),
                bookingOption.getDescription(),
                bookingOption.getCapacity(),
                bookingOption.getPrice(),
                bookingOption.getIsActive()
        );
    }
}
