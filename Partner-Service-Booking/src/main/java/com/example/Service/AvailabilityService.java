package com.example.service;

import com.example.Enum.InventoryStatus;
import com.example.Enum.PartnerErrorCode;
import com.example.Enum.ServiceType;
import com.example.Exception.AppException;
import com.example.Model.BookingOption;
import com.example.Model.Inventory;
import com.example.Model.Venue;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.InventoryRepository;
import com.example.Repository.VenueRepository;
import com.example.dto.Response.AvailabilityItemResponse;
import com.example.dto.Response.AvailabilitySearchResponse;
import com.example.dto.Request.RoomAvailabilityRequest;
import com.example.dto.Request.SlotAvailabilityRequest;
import com.example.dto.Request.TicketAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final VenueRepository venueRepository;
    private final BookingOptionRepository bookingOptionRepository;
    private final InventoryRepository inventoryRepository;

    public AvailabilitySearchResponse searchRooms(RoomAvailabilityRequest request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new AppException(PartnerErrorCode.INVALID_DATE_RANGE);
        }

        List<Venue> venues = resolveVenues(request.venueId(), ServiceType.ROOM);
        List<BookingOption> options = resolveOptions(
                venues,
                request.quantity(),
                request.guestCount(),
                request.maxPrice()
        );

        if (options.isEmpty()) {
            return emptyResponse(ServiceType.ROOM);
        }

        List<String> optionIds = optionIds(options);
        List<Inventory> inventories = inventoryRepository
                .findAllByOptionIdInAndServiceDateGreaterThanEqualAndServiceDateLessThanAndStatus(
                        optionIds,
                        request.checkInDate(),
                        request.checkOutDate(),
                        InventoryStatus.OPEN
                );

        Map<String, Map<LocalDate, Integer>> quantityByOptionAndDate = new HashMap<>();
        for (Inventory inventory : inventories) {
            quantityByOptionAndDate
                    .computeIfAbsent(inventory.getOptionId(), ignored -> new HashMap<>())
                    .merge(inventory.getServiceDate(), availableQuantity(inventory), Math::max);
        }

        long numberOfNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        Map<String, Venue> venueById = venueMap(venues);
        List<AvailabilityItemResponse> results = new ArrayList<>();

        for (BookingOption option : sortedOptions(options)) {
            Map<LocalDate, Integer> quantitiesByDate = quantityByOptionAndDate
                    .getOrDefault(option.getOptionId(), Map.of());

            int minimumAvailable = Integer.MAX_VALUE;
            boolean availableForEveryNight = true;
            for (LocalDate date = request.checkInDate(); date.isBefore(request.checkOutDate()); date = date.plusDays(1)) {
                int available = quantitiesByDate.getOrDefault(date, 0);
                if (available < request.quantity()) {
                    availableForEveryNight = false;
                    break;
                }
                minimumAvailable = Math.min(minimumAvailable, available);
            }

            if (!availableForEveryNight) {
                continue;
            }

            Venue venue = venueById.get(option.getVenueId());
            BigDecimal totalAmount = option.getPrice()
                    .multiply(BigDecimal.valueOf(request.quantity()))
                    .multiply(BigDecimal.valueOf(numberOfNights));

            results.add(new AvailabilityItemResponse(
                    venue.getProviderId(),
                    venue.getVenueId(),
                    venue.getName(),
                    option.getOptionId(),
                    option.getName(),
                    ServiceType.ROOM,
                    option.getCapacity(),
                    request.checkInDate(),
                    request.checkOutDate(),
                    null,
                    null,
                    null,
                    minimumAvailable,
                    option.getPrice(),
                    totalAmount,
                    DEFAULT_CURRENCY
            ));
        }

        return new AvailabilitySearchResponse(ServiceType.ROOM, results);
    }

    public AvailabilitySearchResponse searchSlots(SlotAvailabilityRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new AppException(PartnerErrorCode.INVALID_TIME_RANGE);
        }

        List<Venue> venues = resolveVenues(request.venueId(), ServiceType.SLOT);
        List<BookingOption> options = resolveOptions(
                venues,
                request.quantity(),
                request.guestCount(),
                request.maxPrice()
        );

        if (options.isEmpty()) {
            return emptyResponse(ServiceType.SLOT);
        }

        List<Inventory> inventories = inventoryRepository
                .findAllByOptionIdInAndServiceDateAndStartTimeAndEndTimeAndStatus(
                        optionIds(options),
                        request.serviceDate(),
                        request.startTime(),
                        request.endTime(),
                        InventoryStatus.OPEN
                );

        Map<String, Integer> quantityByOption = inventories.stream()
                .collect(Collectors.toMap(
                        Inventory::getOptionId,
                        AvailabilityService::availableQuantity,
                        Math::max,
                        LinkedHashMap::new
                ));

        Map<String, Venue> venueById = venueMap(venues);
        List<AvailabilityItemResponse> results = new ArrayList<>();
        for (BookingOption option : sortedOptions(options)) {
            int available = quantityByOption.getOrDefault(option.getOptionId(), 0);
            if (available < request.quantity()) {
                continue;
            }

            Venue venue = venueById.get(option.getVenueId());
            results.add(new AvailabilityItemResponse(
                    venue.getProviderId(),
                    venue.getVenueId(),
                    venue.getName(),
                    option.getOptionId(),
                    option.getName(),
                    ServiceType.SLOT,
                    option.getCapacity(),
                    null,
                    null,
                    request.serviceDate(),
                    request.startTime(),
                    request.endTime(),
                    available,
                    option.getPrice(),
                    option.getPrice().multiply(BigDecimal.valueOf(request.quantity())),
                    DEFAULT_CURRENCY
            ));
        }

        return new AvailabilitySearchResponse(ServiceType.SLOT, results);
    }

    public AvailabilitySearchResponse searchTickets(TicketAvailabilityRequest request) {
        if (request.endTime() != null
                && (request.startTime() == null || !request.endTime().isAfter(request.startTime()))) {
            throw new AppException(PartnerErrorCode.INVALID_TIME_RANGE);
        }

        List<Venue> venues = resolveVenues(request.venueId(), ServiceType.TICKET);
        List<BookingOption> options = resolveOptions(
                venues,
                request.quantity(),
                null,
                request.maxPrice()
        );

        if (options.isEmpty()) {
            return emptyResponse(ServiceType.TICKET);
        }

        List<Inventory> inventories = inventoryRepository.findAllByOptionIdInAndServiceDateAndStatus(
                optionIds(options),
                request.serviceDate(),
                InventoryStatus.OPEN
        );

        Map<String, BookingOption> optionById = options.stream()
                .collect(Collectors.toMap(BookingOption::getOptionId, Function.identity()));
        Map<String, Venue> venueById = venueMap(venues);
        List<AvailabilityItemResponse> results = new ArrayList<>();

        inventories.stream()
                .filter(inventory -> availableQuantity(inventory) >= request.quantity())
                .filter(inventory -> request.startTime() == null || request.startTime().equals(inventory.getStartTime()))
                .filter(inventory -> request.endTime() == null || request.endTime().equals(inventory.getEndTime()))
                .sorted(Comparator.comparing(
                        Inventory::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .forEach(inventory -> {
                    BookingOption option = optionById.get(inventory.getOptionId());
                    if (option == null) {
                        return;
                    }
                    Venue venue = venueById.get(option.getVenueId());
                    results.add(new AvailabilityItemResponse(
                            venue.getProviderId(),
                            venue.getVenueId(),
                            venue.getName(),
                            option.getOptionId(),
                            option.getName(),
                            ServiceType.TICKET,
                            option.getCapacity(),
                            null,
                            null,
                            inventory.getServiceDate(),
                            inventory.getStartTime(),
                            inventory.getEndTime(),
                            availableQuantity(inventory),
                            option.getPrice(),
                            option.getPrice().multiply(BigDecimal.valueOf(request.quantity())),
                            DEFAULT_CURRENCY
                    ));
                });

        return new AvailabilitySearchResponse(ServiceType.TICKET, results);
    }

    private List<Venue> resolveVenues(String venueId, ServiceType serviceType) {
        if (venueId != null && !venueId.isBlank()) {
            Venue venue = venueRepository.findByVenueIdAndIsActiveTrue(venueId)
                    .orElseThrow(() -> new AppException(PartnerErrorCode.VENUE_NOT_FOUND));
            if (venue.getServiceType() != serviceType) {
                throw new AppException(PartnerErrorCode.VENUE_SERVICE_TYPE_MISMATCH);
            }
            return List.of(venue);
        }

        return venueRepository.findAllByServiceTypeAndIsActiveTrue(serviceType);
    }

    private List<BookingOption> resolveOptions(
            List<Venue> venues,
            int requestedQuantity,
            Integer guestCount,
            BigDecimal maxPrice
    ) {
        if (venues.isEmpty()) {
            return List.of();
        }

        List<String> venueIds = venues.stream().map(Venue::getVenueId).toList();
        return bookingOptionRepository.findAllByVenueIdInAndIsActiveTrue(venueIds).stream()
                .filter(option -> option.getPrice() != null)
                .filter(option -> maxPrice == null || option.getPrice().compareTo(maxPrice) <= 0)
                .filter(option -> guestCount == null
                        || option.getCapacity() == null
                        || option.getCapacity() * requestedQuantity >= guestCount)
                .toList();
    }

    private static List<String> optionIds(List<BookingOption> options) {
        return options.stream().map(BookingOption::getOptionId).toList();
    }

    private static Map<String, Venue> venueMap(List<Venue> venues) {
        return venues.stream().collect(Collectors.toMap(Venue::getVenueId, Function.identity()));
    }

    private static List<BookingOption> sortedOptions(List<BookingOption> options) {
        return options.stream()
                .sorted(Comparator.comparing(
                        BookingOption::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
    }

    private static int availableQuantity(Inventory inventory) {
        return inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
    }

    private static AvailabilitySearchResponse emptyResponse(ServiceType serviceType) {
        return new AvailabilitySearchResponse(serviceType, List.of());
    }
}
