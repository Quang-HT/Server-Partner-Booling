package com.example.Repository;

import com.example.Model.BookingOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookingOptionRepository extends JpaRepository<BookingOption, String> {
    public List<BookingOption> findAllByVenueId(String venueId);
    List<BookingOption> findAllByVenueIdInAndIsActiveTrue(Collection<String> venueIds);
}
