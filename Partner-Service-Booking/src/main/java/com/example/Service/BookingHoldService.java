package com.example.Service;

import com.example.Enum.BookingHoldStatus;
import com.example.Enum.InventoryStatus;
import com.example.Enum.PartnerErrorCode;
import com.example.Enum.ServiceType;
import com.example.Exception.AppException;
import com.example.Mapper.HoldCheckMapper;
import com.example.Model.BookingHold;
import com.example.Model.BookingHoldItem;
import com.example.Model.BookingOption;
import com.example.Model.Inventory;
import com.example.Model.Venue;
import com.example.Repository.BookingHoldItemRepository;
import com.example.Repository.BookingHoldRepository;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.InventoryRepository;
import com.example.Repository.VenueRepository;
import com.example.dto.Request.BookingHoldRequest;
import com.example.dto.Response.BookingHoldResponse;
import com.example.dto.Response.HoldStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingHoldService {
    private static final String DEFAULT_CURRENCY = "VND";

    private final BookingHoldRepository bookingHoldRepository;
    private final BookingHoldItemRepository bookingHoldItemRepository;
    private final BookingOptionRepository bookingOptionRepository;
    private final VenueRepository venueRepository;
    private final InventoryRepository inventoryRepository;
    private final HoldInventoryService holdInventoryService;
    private final HoldCheckMapper holdCheckMapper;

    @Transactional
    public BookingHoldResponse holdBooking(BookingHoldRequest bookingHoldRequest) {
        validateCommonRequest(bookingHoldRequest);

        BookingOption bookingOption = bookingOptionRepository.findById(bookingHoldRequest.getOptionId()).orElseThrow(
                () -> new AppException(PartnerErrorCode.OPTION_NOT_FOUND)
        );
        Venue venue = venueRepository.findById(bookingOption.getVenueId()).orElseThrow(
                () -> new AppException(PartnerErrorCode.VENUE_NOT_FOUND)
        );

        if (!Boolean.TRUE.equals(bookingOption.getIsActive())) {
            throw new AppException(PartnerErrorCode.OPTION_UNAVAILABLE);
        }

        if (!Boolean.TRUE.equals(venue.getIsActive())) {
            throw new AppException(PartnerErrorCode.OPTION_UNAVAILABLE);
        }

        if (bookingHoldRequest.getGuestCount() != null) {
            if (bookingOption.getCapacity() == null || bookingHoldRequest.getQuantity() == null) {
                throw new AppException(PartnerErrorCode.INVALID_REQUEST);
            }

            long totalCapacity = (long) bookingOption.getCapacity()
                    * bookingHoldRequest.getQuantity();

            if (bookingHoldRequest.getGuestCount() > totalCapacity) {
                throw new AppException(PartnerErrorCode.OPTION_UNAVAILABLE);
            }
        }

        List<Inventory> inventories = List.of();

        switch (venue.getServiceType()) {
                case ROOM:
                    if (bookingHoldRequest.getCheckInDate() == null
                            || bookingHoldRequest.getCheckOutDate() == null
                            || !bookingHoldRequest.getCheckOutDate().isAfter(bookingHoldRequest.getCheckInDate())) {
                        throw new AppException(PartnerErrorCode.INVALID_DATE_RANGE);
                    }
                    inventories = inventoryRepository.findRoomInventoriesForUpdate(
                            bookingHoldRequest.getOptionId(),
                            bookingHoldRequest.getCheckInDate(),
                            bookingHoldRequest.getCheckOutDate()
                    );
                    break;

                case SLOT:
                    if (bookingHoldRequest.getServiceDate() == null
                            || bookingHoldRequest.getStartTime() == null
                            || bookingHoldRequest.getEndTime() == null
                            || !bookingHoldRequest.getEndTime().isAfter(bookingHoldRequest.getStartTime())) {
                        throw new AppException(PartnerErrorCode.INVALID_TIME_RANGE);
                    }
                    inventories = List.of(inventoryRepository.findSlotInventoryForUpdate(
                                    bookingHoldRequest.getOptionId(),
                                    bookingHoldRequest.getServiceDate(),
                                    bookingHoldRequest.getStartTime(),
                                    bookingHoldRequest.getEndTime()
                            ).orElseThrow(() -> new AppException(PartnerErrorCode.INVENTORY_NOT_FOUND))
                    );
                    break;

                case TICKET:
                    if (bookingHoldRequest.getServiceDate() == null
                            || bookingHoldRequest.getStartTime() == null) {
                        throw new AppException(PartnerErrorCode.INVALID_REQUEST);
                    }
                    inventories = List.of(inventoryRepository.findTicketInventoryForUpdate(
                                    bookingHoldRequest.getOptionId(),
                                    bookingHoldRequest.getServiceDate(),
                                    bookingHoldRequest.getStartTime()
                            ).orElseThrow(() -> new AppException(PartnerErrorCode.INVENTORY_NOT_FOUND))
                    );
                    break;

                default:
                    throw new AppException(PartnerErrorCode.INVALID_REQUEST);
            }

        validateInventories(inventories, bookingHoldRequest.getQuantity());
        if (venue.getServiceType() == ServiceType.ROOM) {
            validateCompleteRoomInventory(inventories, bookingHoldRequest);
        }
        decreaseInventory(inventories, bookingHoldRequest.getQuantity());

        BigDecimal unitPrice = bookingOption.getPrice();
        if (unitPrice == null) {
            throw new AppException(PartnerErrorCode.INVALID_REQUEST);
        }

        long billableUnits = venue.getServiceType() == ServiceType.ROOM
                ? inventories.size()
                : 1;

        BigDecimal totalPrice = unitPrice
                .multiply(BigDecimal.valueOf(bookingHoldRequest.getQuantity()))
                .multiply(BigDecimal.valueOf(billableUnits));

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        BookingHold hold = new BookingHold();
        hold.setOptionId(bookingOption.getOptionId());
        hold.setInventoryId(inventories.size() == 1 ? inventories.getFirst().getInventoryId() : null);
        hold.setQuantity(bookingHoldRequest.getQuantity());
        hold.setUnitPrice(unitPrice);
        hold.setTotalAmount(totalPrice);
        hold.setCurrency(DEFAULT_CURRENCY);
        hold.setStatus(BookingHoldStatus.HELD);
        hold.setExpiresAt(expiresAt);

        BookingHold savedHold = bookingHoldRepository.save(hold);
        saveHoldItems(savedHold, inventories, bookingHoldRequest.getQuantity());

        return new BookingHoldResponse(
                savedHold.getHoldId(),
                bookingOption.getOptionId(),
                savedHold.getStatus(),
                savedHold.getQuantity(),
                unitPrice,
                totalPrice,
                savedHold.getCurrency(),
                savedHold.getExpiresAt()
        );
    }


    @Transactional
    public HoldStatusResponse getStatusHold(String holdId) {
        BookingHold hold = findHoldForUpdate(holdId);
        expireIfNecessary(hold, LocalDateTime.now());
        return toStatusResponse(hold);
    }

    @Transactional
    public HoldStatusResponse releaseHold(String holdId) {
        BookingHold hold = findHoldForUpdate(holdId);
        LocalDateTime now = LocalDateTime.now();

        if (hold.getStatus() == BookingHoldStatus.HELD && isExpired(hold, now)) {
            holdInventoryService.restore(hold);
            hold.setStatus(BookingHoldStatus.EXPIRED);
            bookingHoldRepository.save(hold);
            return toStatusResponse(hold);
        }

        if (hold.getStatus() == BookingHoldStatus.CONFIRMED) {
            throw new AppException(PartnerErrorCode.HOLD_ALREADY_USED);
        }

        if (hold.getStatus() == BookingHoldStatus.HELD) {
            holdInventoryService.restore(hold);
            hold.setStatus(BookingHoldStatus.RELEASED);
            bookingHoldRepository.save(hold);
        }

        return toStatusResponse(hold);
    }

    private void validateCommonRequest(BookingHoldRequest request) {
        if (request == null
                || request.getOptionId() == null
                || request.getOptionId().isBlank()
                || request.getQuantity() == null
                || request.getQuantity() <= 0) {
            throw new AppException(PartnerErrorCode.INVALID_REQUEST);
        }
    }

    private void validateInventories(List<Inventory> inventories, Integer quantity) {
        if (inventories == null || inventories.isEmpty()) {
            throw new AppException(PartnerErrorCode.INVENTORY_NOT_FOUND);
        }

        for (Inventory inventory : inventories) {
            int available = inventory.getAvailableQuantity() == null
                    ? 0
                    : inventory.getAvailableQuantity();
            if (inventory.getStatus() != InventoryStatus.OPEN || available < quantity) {
                throw new AppException(PartnerErrorCode.OPTION_UNAVAILABLE);
            }
        }
    }

    private void validateCompleteRoomInventory(
            List<Inventory> inventories,
            BookingHoldRequest request
    ) {
        long expectedNights = ChronoUnit.DAYS.between(
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        Set<LocalDate> inventoryDates = new HashSet<>();
        for (Inventory inventory : inventories) {
            inventoryDates.add(inventory.getServiceDate());
        }

        if (inventories.size() != expectedNights || inventoryDates.size() != expectedNights) {
            throw new AppException(PartnerErrorCode.OPTION_UNAVAILABLE);
        }
    }

    private void decreaseInventory(List<Inventory> inventories, int quantity) {
        for (Inventory inventory : inventories) {
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        }
        inventoryRepository.saveAll(inventories);
    }

    private void saveHoldItems(
            BookingHold hold,
            List<Inventory> inventories,
            int quantity
    ) {
        List<BookingHoldItem> items = inventories.stream()
                .map(inventory -> {
                    BookingHoldItem item = new BookingHoldItem();
                    item.setHoldId(hold.getHoldId());
                    item.setInventoryId(inventory.getInventoryId());
                    item.setQuantity(quantity);
                    return item;
                })
                .toList();
        bookingHoldItemRepository.saveAll(items);
    }

    private BookingHold findHoldForUpdate(String holdId) {
        if (holdId == null || holdId.isBlank()) {
            throw new AppException(PartnerErrorCode.INVALID_REQUEST);
        }
        return bookingHoldRepository.findByIdForUpdate(holdId)
                .orElseThrow(() -> new AppException(PartnerErrorCode.HOLD_NOT_FOUND));
    }

    private void expireIfNecessary(BookingHold hold, LocalDateTime now) {
        if (hold.getStatus() == BookingHoldStatus.HELD && isExpired(hold, now)) {
            holdInventoryService.restore(hold);
            hold.setStatus(BookingHoldStatus.EXPIRED);
            bookingHoldRepository.save(hold);
        }
    }

    private boolean isExpired(BookingHold hold, LocalDateTime now) {
        return hold.getExpiresAt() == null || !hold.getExpiresAt().isAfter(now);
    }

    private HoldStatusResponse toStatusResponse(BookingHold hold) {
        long seconds = hold.getStatus() == BookingHoldStatus.HELD
                ? Math.max(0, Duration.between(LocalDateTime.now(), hold.getExpiresAt()).getSeconds())
                : 0;
        int remainingSeconds = (int) Math.min(Integer.MAX_VALUE, seconds);
        return holdCheckMapper.toResponse(
                hold,
                resolveTotalAmount(hold),
                remainingSeconds
        );
    }

    private BigDecimal resolveTotalAmount(BookingHold hold) {
        if (hold.getTotalAmount() != null) {
            return hold.getTotalAmount();
        }

        BookingOption option = bookingOptionRepository.findById(hold.getOptionId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.OPTION_NOT_FOUND));
        return option.getPrice().multiply(BigDecimal.valueOf(hold.getQuantity()));
    }
}
