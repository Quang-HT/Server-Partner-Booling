package com.example.Controller;

import com.example.Enum.ServiceType;
import com.example.Exception.GlobalExceptionHandler;
import com.example.dto.Response.AvailabilitySearchResponse;
import com.example.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvailabilityControllerTest {

    private AvailabilityService availabilityService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        availabilityService = mock(AvailabilityService.class);
        AvailabilityController controller = new AvailabilityController(availabilityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void roomSearchEndpointReturnsSuccess() throws Exception {
        when(availabilityService.searchRooms(any()))
                .thenReturn(new AvailabilitySearchResponse(ServiceType.ROOM, List.of()));

        mockMvc.perform(post("/api/v1/availability/rooms/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkInDate": "2099-09-10",
                                  "checkOutDate": "2099-09-12",
                                  "quantity": 1,
                                  "guestCount": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceType").value("ROOM"));
    }

    @Test
    void slotSearchEndpointReturnsSuccess() throws Exception {
        when(availabilityService.searchSlots(any()))
                .thenReturn(new AvailabilitySearchResponse(ServiceType.SLOT, List.of()));

        mockMvc.perform(post("/api/v1/availability/slots/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceDate": "2099-09-10",
                                  "startTime": "19:00:00",
                                  "endTime": "21:00:00",
                                  "quantity": 1,
                                  "guestCount": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceType").value("SLOT"));
    }

    @Test
    void ticketSearchEndpointReturnsSuccess() throws Exception {
        when(availabilityService.searchTickets(any()))
                .thenReturn(new AvailabilitySearchResponse(ServiceType.TICKET, List.of()));

        mockMvc.perform(post("/api/v1/availability/tickets/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceDate": "2099-09-10",
                                  "startTime": "20:00:00",
                                  "endTime": "22:00:00",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceType").value("TICKET"));
    }
}
