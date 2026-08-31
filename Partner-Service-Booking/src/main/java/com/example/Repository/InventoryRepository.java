package com.example.Repository;

import com.example.Enum.InventoryStatus;
import com.example.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    List<Inventory> findAllByOptionIdInAndServiceDateGreaterThanEqualAndServiceDateLessThanAndStatus(
            Collection<String> optionIds,
            LocalDate fromDate,
            LocalDate toDate,
            InventoryStatus status
    );

    List<Inventory> findAllByOptionIdInAndServiceDateAndStartTimeAndEndTimeAndStatus(
            Collection<String> optionIds,
            LocalDate serviceDate,
            LocalTime startTime,
            LocalTime endTime,
            InventoryStatus status
    );

    List<Inventory> findAllByOptionIdInAndServiceDateAndStatus(
            Collection<String> optionIds,
            LocalDate serviceDate,
            InventoryStatus status
    );
}
