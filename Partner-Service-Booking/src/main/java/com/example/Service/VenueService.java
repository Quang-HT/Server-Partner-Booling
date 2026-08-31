package com.example.Service;

import com.example.Enum.ServiceType;
import com.example.Mapper.BookingOptionMapper;
import com.example.Mapper.VenueMapper;
import com.example.Model.BookingOption;
import com.example.Model.Venue;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.Specification.VenueSpecification;
import com.example.Repository.VenueRepository;
import com.example.dto.Response.BookingOptionResponse;
import com.example.dto.Response.VenueReponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;
    private final BookingOptionRepository bookingOptionRepository;
    private final BookingOptionMapper bookingOptionMapper;

    public List<VenueReponse> searchVenue(
            String keyword,
            ServiceType serviceType
    ){
        Specification<Venue> specification = Specification.where(VenueSpecification.isActive())
                .and(VenueSpecification.hasKeyword(keyword))
                .and(VenueSpecification.serviceType(serviceType));

        List<Venue> result = venueRepository.findAll(specification);

        return result.stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    public VenueReponse detailVenue(String venueId){
        Venue venueDetail = venueRepository.findById(venueId).orElseThrow(()-> new RuntimeException("NOT FOUND VENUE"));
        VenueReponse result= venueMapper.toResponse(venueDetail);
        return result;
    }

    public List<BookingOptionResponse> findAllBookingOptionByVenueId(String venueId){
        List<BookingOption> bookingOptions = bookingOptionRepository.findAllByVenueId(venueId);
        return bookingOptions.stream().map(bookingOptionMapper::toResponse).toList();
    }
}
