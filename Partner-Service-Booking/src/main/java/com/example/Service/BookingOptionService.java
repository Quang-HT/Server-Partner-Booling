package com.example.Service;

import com.example.Mapper.BookingOptionMapper;
import com.example.Model.BookingOption;
import com.example.Repository.BookingOptionRepository;
import com.example.dto.Response.BookingOptionResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingOptionService {
    private final BookingOptionRepository bookingOptionRepository;
    private final BookingOptionMapper bookingOptionMapper;

    public BookingOptionResponse getDetailBookingOption(String bookingOptionId){
        BookingOption bookingOption = bookingOptionRepository.findById(bookingOptionId).orElseThrow(()->new RuntimeException("NOT FOUND BOOKING OPTION"));
        BookingOptionResponse bookingOptionResponse = bookingOptionMapper.toResponse(bookingOption);

        return bookingOptionResponse;
    }
}
