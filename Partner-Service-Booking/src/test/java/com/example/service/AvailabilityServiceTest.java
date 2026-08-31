package com.example.service;

import com.example.Enum.InventoryStatus;
import com.example.Enum.ServiceType;
import com.example.Model.BookingOption;
import com.example.Model.Inventory;
import com.example.Model.Venue;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.InventoryRepository;
import com.example.Repository.VenueRepository;
import com.example.dto.Response.AvailabilitySearchResponse;
import com.example.dto.Request.RoomAvailabilityRequest;
import com.example.dto.Request.SlotAvailabilityRequest;
import com.example.dto.Request.TicketAvailabilityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private BookingOptionRepository bookingOptionRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(
                venueRepository,
                bookingOptionRepository,
                inventoryRepository
        );
    }

    @Test
    void roomIsAvailableOnlyWhenEveryNightHasEnoughInventory() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);
        Venue venue = venue("venue-room", ServiceType.ROOM);
        BookingOption option = option("room-deluxe", venue.getVenueId(), 2, "1500000");

        when(venueRepository.findByVenueIdAndIsActiveTrue(venue.getVenueId()))
                .thenReturn(Optional.of(venue));
        when(bookingOptionRepository.findAllByVenueIdInAndIsActiveTrue(anyCollection()))
                .thenReturn(List.of(option));
        when(inventoryRepository
                .findAllByOptionIdInAndServiceDateGreaterThanEqualAndServiceDateLessThanAndStatus(
                        anyCollection(), eq(checkIn), eq(checkOut), eq(InventoryStatus.OPEN)
                ))
                .thenReturn(List.of(
                        inventory("inventory-1", option.getOptionId(), checkIn, null, null, 4),
                        inventory("inventory-2", option.getOptionId(), checkIn.plusDays(1), null, null, 3)
                ));

        AvailabilitySearchResponse response = availabilityService.searchRooms(
                new RoomAvailabilityRequest(venue.getVenueId(), checkIn, checkOut, 1, 2, null)
        );

        assertEquals(ServiceType.ROOM, response.serviceType());
        assertEquals(1, response.options().size());
        assertEquals(3, response.options().getFirst().availableQuantity());
        assertEquals(new BigDecimal("3000000"), response.options().getFirst().totalAmount());
    }

    @Test
    void slotSearchMatchesExactDateAndTime() {
        LocalDate serviceDate = LocalDate.now().plusDays(10);
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        Venue venue = venue("venue-slot", ServiceType.SLOT);
        BookingOption option = option("table-four", venue.getVenueId(), 4, "200000");

        when(venueRepository.findByVenueIdAndIsActiveTrue(venue.getVenueId()))
                .thenReturn(Optional.of(venue));
        when(bookingOptionRepository.findAllByVenueIdInAndIsActiveTrue(anyCollection()))
                .thenReturn(List.of(option));
        when(inventoryRepository.findAllByOptionIdInAndServiceDateAndStartTimeAndEndTimeAndStatus(
                anyCollection(), eq(serviceDate), eq(startTime), eq(endTime), eq(InventoryStatus.OPEN)
        )).thenReturn(List.of(
                inventory("inventory-slot", option.getOptionId(), serviceDate, startTime, endTime, 2)
        ));

        AvailabilitySearchResponse response = availabilityService.searchSlots(
                new SlotAvailabilityRequest(
                        venue.getVenueId(), serviceDate, startTime, endTime, 1, 4, null
                )
        );

        assertEquals(ServiceType.SLOT, response.serviceType());
        assertEquals(1, response.options().size());
        assertEquals(startTime, response.options().getFirst().startTime());
        assertEquals(new BigDecimal("200000"), response.options().getFirst().totalAmount());
    }

    @Test
    void ticketSearchCanFilterByShowTime() {
        LocalDate serviceDate = LocalDate.now().plusDays(10);
        LocalTime startTime = LocalTime.of(20, 0);
        LocalTime endTime = LocalTime.of(22, 0);
        Venue venue = venue("venue-ticket", ServiceType.TICKET);
        BookingOption option = option("ticket-vip", venue.getVenueId(), 1, "1000000");

        when(venueRepository.findByVenueIdAndIsActiveTrue(venue.getVenueId()))
                .thenReturn(Optional.of(venue));
        when(bookingOptionRepository.findAllByVenueIdInAndIsActiveTrue(anyCollection()))
                .thenReturn(List.of(option));
        when(inventoryRepository.findAllByOptionIdInAndServiceDateAndStatus(
                anyCollection(), eq(serviceDate), eq(InventoryStatus.OPEN)
        )).thenReturn(List.of(
                inventory("inventory-ticket", option.getOptionId(), serviceDate, startTime, endTime, 20)
        ));

        AvailabilitySearchResponse response = availabilityService.searchTickets(
                new TicketAvailabilityRequest(
                        venue.getVenueId(), serviceDate, startTime, endTime, 2, null
                )
        );

        assertEquals(ServiceType.TICKET, response.serviceType());
        assertEquals(1, response.options().size());
        assertEquals(20, response.options().getFirst().availableQuantity());
        assertEquals(new BigDecimal("2000000"), response.options().getFirst().totalAmount());
    }

    private static Venue venue(String venueId, ServiceType serviceType) {
        Venue venue = new Venue();
        venue.setVenueId(venueId);
        venue.setProviderId("provider-01");
        venue.setName("Demo venue");
        venue.setServiceType(serviceType);
        venue.setIsActive(true);
        return venue;
    }

    private static BookingOption option(
            String optionId,
            String venueId,
            int capacity,
            String price
    ) {
        BookingOption option = new BookingOption();
        option.setOptionId(optionId);
        option.setVenueId(venueId);
        option.setName("Demo option");
        option.setCapacity(capacity);
        option.setPrice(new BigDecimal(price));
        option.setIsActive(true);
        return option;
    }

    private static Inventory inventory(
            String inventoryId,
            String optionId,
            LocalDate serviceDate,
            LocalTime startTime,
            LocalTime endTime,
            int availableQuantity
    ) {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(inventoryId);
        inventory.setOptionId(optionId);
        inventory.setServiceDate(serviceDate);
        inventory.setStartTime(startTime);
        inventory.setEndTime(endTime);
        inventory.setAvailableQuantity(availableQuantity);
        inventory.setStatus(InventoryStatus.OPEN);
        return inventory;
    }
}
