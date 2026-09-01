package com.example.Repository;

import com.example.Model.BookingHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookingHoldRepository extends JpaRepository<BookingHold, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM BookingHold h WHERE h.holdId = :holdId")
    Optional<BookingHold> findByIdForUpdate(@Param("holdId") String holdId);
}
