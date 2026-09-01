package com.example.Repository;

import com.example.Enum.InventoryStatus;
import com.example.Model.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    List<Inventory> findAllByInventoryIdIn(Collection<String> inventoryIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.inventoryId IN :inventoryIds ORDER BY i.inventoryId")
    List<Inventory> findAllByInventoryIdInForUpdate(
            @Param("inventoryIds") Collection<String> inventoryIds
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT i FROM Inventory i
    WHERE i.optionId = :optionId
      AND i.serviceDate = :serviceDate
      AND i.startTime = :startTime
      AND i.endTime = :endTime
""")
    Optional<Inventory> findSlotInventoryForUpdate(
            @Param("optionId") String optionId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT i FROM Inventory i
    WHERE i.optionId = :optionId
        AND i.serviceDate = :serviceDate
        AND i.startTime = :startTime
            """)
    Optional<Inventory> findTicketInventoryForUpdate(
            @Param("optionId") String optionId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("startTime") LocalTime startTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT i FROM Inventory i
    WHERE i.optionId = :optionId
      AND i.serviceDate >= :checkInDate
      AND i.serviceDate < :checkOutDate
    ORDER BY i.serviceDate
""")
    List<Inventory> findRoomInventoriesForUpdate(
            @Param("optionId") String optionId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );
}
