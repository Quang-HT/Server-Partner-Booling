package com.example.Repository;

import com.example.Model.PartnerBooking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerBookingRepository extends JpaRepository<PartnerBooking, String> {

    Optional<PartnerBooking> findByHoldId(String holdId);

    Optional<PartnerBooking> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM PartnerBooking b WHERE b.bookingId = :bookingId")
    Optional<PartnerBooking> findByIdForUpdate(@Param("bookingId") String bookingId);
}
