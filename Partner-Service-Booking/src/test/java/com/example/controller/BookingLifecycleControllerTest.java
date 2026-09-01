package com.example.Controller;

import com.example.Enum.InventoryStatus;
import com.example.Enum.BookingHoldStatus;
import com.example.Enum.ServiceType;
import com.example.Model.BookingHold;
import com.example.Model.BookingOption;
import com.example.Model.Inventory;
import com.example.Model.Venue;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.BookingHoldRepository;
import com.example.Repository.InventoryRepository;
import com.example.Repository.PartnerBookingRepository;
import com.example.Repository.VenueRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking_lifecycle_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class BookingLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private BookingOptionRepository bookingOptionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PartnerBookingRepository partnerBookingRepository;

    @Autowired
    private BookingHoldRepository bookingHoldRepository;

    @Test
    void holdStatusConfirmGetCancelAndReleaseWorkEndToEnd() throws Exception {
        LocalDate serviceDate = LocalDate.now().plusDays(10);
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        TestData testData = prepareSlotInventory(serviceDate, startTime, endTime);

        String firstHoldId = createHold(testData, serviceDate, startTime, endTime);

        mockMvc.perform(get("/api/v1/holds/{holdId}", firstHoldId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HELD"))
                .andExpect(jsonPath("$.data.totalAmount").value(200000))
                .andExpect(jsonPath("$.data.remainingSeconds", greaterThan(0)));

        String confirmBody = """
                {
                  "holdId": "%s",
                  "customerRef": "customer-001",
                  "idempotencyKey": "confirm-request-001"
                }
                """.formatted(firstHoldId);

        String confirmResponse = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.inventories[0].inventoryId").value(testData.inventoryId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String bookingId = JsonPath.read(confirmResponse, "$.data.providerBookingId");

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providerBookingId").value(bookingId));
        assertEquals(1, partnerBookingRepository.count());

        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providerId").value("provider-slot-001"))
                .andExpect(jsonPath("$.data.optionId").value(testData.optionId()))
                .andExpect(jsonPath("$.data.totalAmount").value(200000));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        assertEquals(5, availableQuantity(testData.inventoryId()));

        // Hủy lại phải idempotent, không được hoàn tồn kho lần thứ hai.
        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        assertEquals(5, availableQuantity(testData.inventoryId()));

        String secondHoldId = createHold(testData, serviceDate, startTime, endTime);
        assertEquals(4, availableQuantity(testData.inventoryId()));

        mockMvc.perform(delete("/api/v1/holds/{holdId}", secondHoldId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.remainingSeconds").value(0));
        assertEquals(5, availableQuantity(testData.inventoryId()));
    }

    @Test
    void expiredHoldCannotBeConfirmedAndItsInventoryIsRestored() throws Exception {
        LocalDate serviceDate = LocalDate.now().plusDays(11);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);
        TestData testData = prepareSlotInventory(serviceDate, startTime, endTime);
        String holdId = createHold(testData, serviceDate, startTime, endTime);
        long bookingCountBefore = partnerBookingRepository.count();

        BookingHold hold = bookingHoldRepository.findById(holdId).orElseThrow();
        hold.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        bookingHoldRepository.save(hold);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "holdId": "%s",
                                  "customerRef": "customer-expired",
                                  "idempotencyKey": "confirm-expired-%s"
                                }
                                """.formatted(holdId, holdId)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("PARTNER_410_001"));

        assertEquals(5, availableQuantity(testData.inventoryId()));
        assertEquals(bookingCountBefore, partnerBookingRepository.count());
        assertEquals(
                BookingHoldStatus.EXPIRED,
                bookingHoldRepository.findById(holdId).orElseThrow().getStatus()
        );
    }

    private String createHold(
            TestData testData,
            LocalDate serviceDate,
            LocalTime startTime,
            LocalTime endTime
    ) throws Exception {
        String response = mockMvc.perform(post("/api/v1/holds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "%s",
                                  "quantity": 1,
                                  "serviceDate": "%s",
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "guestCount": 4
                                }
                                """.formatted(testData.optionId(), serviceDate, startTime, endTime)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HELD"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.data.holdId");
    }

    private TestData prepareSlotInventory(
            LocalDate serviceDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        Venue venue = new Venue();
        venue.setProviderId("provider-slot-001");
        venue.setName("Demo restaurant");
        venue.setServiceType(ServiceType.SLOT);
        venue.setIsActive(true);
        Venue savedVenue = venueRepository.save(venue);

        BookingOption option = new BookingOption();
        option.setVenueId(savedVenue.getVenueId());
        option.setName("Table for four");
        option.setCapacity(4);
        option.setPrice(new BigDecimal("200000"));
        option.setIsActive(true);
        BookingOption savedOption = bookingOptionRepository.save(option);

        Inventory inventory = new Inventory();
        inventory.setOptionId(savedOption.getOptionId());
        inventory.setServiceDate(serviceDate);
        inventory.setStartTime(startTime);
        inventory.setEndTime(endTime);
        inventory.setTotalQuantity(5);
        inventory.setAvailableQuantity(5);
        inventory.setStatus(InventoryStatus.OPEN);
        Inventory savedInventory = inventoryRepository.save(inventory);

        return new TestData(savedOption.getOptionId(), savedInventory.getInventoryId());
    }

    private int availableQuantity(String inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow()
                .getAvailableQuantity();
    }

    private record TestData(String optionId, String inventoryId) {
    }
}
