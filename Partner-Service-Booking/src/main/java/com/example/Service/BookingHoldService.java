package com.example.Service;

import com.example.Enum.BookingHoldStatus;
import com.example.Enum.InventoryStatus;
import com.example.Enum.ServiceType;
import com.example.Model.BookingHold;
import com.example.Model.BookingOption;
import com.example.Model.Inventory;
import com.example.Model.Venue;
import com.example.Repository.BookingHoldRepository;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.InventoryRepository;
import com.example.Repository.VenueRepository;
import com.example.dto.Request.BookingHoldRequest;
import com.example.dto.Response.BookingHoldResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingHoldService {
    private final BookingHoldRepository bookingHoldRepository;
    private final BookingOptionRepository bookingOptionRepository;
    private final VenueRepository venueRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public BookingHoldResponse holdBooking(BookingHoldRequest bookingHoldRequest) {
        BookingOption bookingOption = bookingOptionRepository.findById(bookingHoldRequest.getOptionId()).orElseThrow(
                () -> new RuntimeException("NOT FOUND BOOKING OPTION")
        );
        Venue venue = venueRepository.findById(bookingOption.getVenueId()).orElseThrow(
                () -> new RuntimeException("NOT FOUND VENUE")
        );

        if (!Boolean.TRUE.equals(bookingOption.getIsActive())) {
            throw new RuntimeException("BOOKING_OPTION_INACTIVE");
        }

        if (!Boolean.TRUE.equals(venue.getIsActive())) {
            throw new RuntimeException("VENUE_INACTIVE");
        }

        if (bookingHoldRequest.getGuestCount() != null) {
            if (bookingOption.getCapacity() == null || bookingHoldRequest.getQuantity() == null) {
                throw new RuntimeException("MISSING_CAPACITY_OR_QUANTITY");
            }

            long totalCapacity = (long) bookingOption.getCapacity()
                    * bookingHoldRequest.getQuantity();

            if (bookingHoldRequest.getGuestCount() > totalCapacity) {
                throw new RuntimeException("GUEST_COUNT_EXCEEDS_CAPACITY");
            }
        }

        List<Inventory> inventories = List.of();

        switch (venue.getServiceType()) {
                case ROOM:
                    inventories = inventoryRepository.findRoomInventoriesForUpdate(
                            bookingHoldRequest.getOptionId(),
                            bookingHoldRequest.getCheckInDate(),
                            bookingHoldRequest.getCheckOutDate()
                    );
                    break;

                case SLOT:
                    inventories = List.of(inventoryRepository.findSlotInventoryForUpdate(
                                    bookingHoldRequest.getOptionId(),
                                    bookingHoldRequest.getServiceDate(),
                                    bookingHoldRequest.getStartTime(),
                                    bookingHoldRequest.getEndTime()
                            ).orElseThrow()
                    );
                    break;

                case TICKET:
                    inventories = List.of(inventoryRepository.findTicketInventoryForUpdate(
                                    bookingHoldRequest.getOptionId(),
                                    bookingHoldRequest.getServiceDate(),
                                    bookingHoldRequest.getStartTime()
                            ).orElseThrow()
                    );
                    break;

                default:
                    throw new RuntimeException("UNSUPPORTED_SERVICE_TYPE");
            }

        validateInventories(inventories, bookingHoldRequest.getQuantity());
        decreaseInventory(inventories, bookingHoldRequest.getQuantity());

        BigDecimal unitPrice = bookingOption.getPrice();

        long billableUnits = venue.getServiceType() == ServiceType.ROOM
                ? inventories.size()
                : 1;

        BigDecimal totalPrice = unitPrice
                .multiply(BigDecimal.valueOf(bookingHoldRequest.getQuantity()))
                .multiply(BigDecimal.valueOf(billableUnits));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        BookingHold hold = new BookingHold();
        hold.setOptionId(bookingOption.getOptionId());
        hold.setQuantity(bookingHoldRequest.getQuantity());
        hold.setStatus(BookingHoldStatus.HELD);
        hold.setExpiresAt(expiresAt);
        hold.setCreatedAt(LocalDateTime.now());

        BookingHold savedHold = bookingHoldRepository.save(hold);

        return new BookingHoldResponse(
                savedHold.getHoldId(),
                bookingOption.getOptionId(),
                savedHold.getStatus(),
                savedHold.getQuantity(),
                unitPrice,
                totalPrice,
                "VNĐ",
                savedHold.getExpiresAt()
        );
    }


    private void validateInventories(
            List<Inventory> inventories,
            Integer quantity
    ) {
        if (inventories == null || inventories.isEmpty()) {
            throw new RuntimeException("INVENTORY_NOT_FOUND");
        }

        for (Inventory inventory : inventories) {
            if (inventory.getStatus() != InventoryStatus.OPEN) {
                throw new RuntimeException(
                        "INVENTORY_NOT_OPEN: " + inventory.getServiceDate()
                );
            }

            if (inventory.getAvailableQuantity() < quantity) {
                throw new RuntimeException(
                        "NOT_ENOUGH_AVAILABILITY: " + inventory.getServiceDate()
                );
            }
        }
    }

    private void decreaseInventory(
            List<Inventory> inventories,
            int quantity
    ) {
        for (Inventory inventory : inventories) {
            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - quantity
            );
        }

        inventoryRepository.saveAll(inventories);
    }
}
