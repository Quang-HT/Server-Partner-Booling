package com.example.Repository;

import com.example.Model.BookingHold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingHoldRepository extends JpaRepository<BookingHold, String> {
}
