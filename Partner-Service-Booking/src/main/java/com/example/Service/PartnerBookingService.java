package com.example.Service;

import com.example.Enum.BookingHoldStatus;
import com.example.Enum.PartnerBookingStatus;
import com.example.Enum.PartnerErrorCode;
import com.example.Exception.AppException;
import com.example.Exception.HoldExpiredException;
import com.example.Model.BookingHold;
import com.example.Model.BookingOption;
import com.example.Model.PartnerBooking;
import com.example.Model.Venue;
import com.example.Repository.BookingHoldRepository;
import com.example.Repository.BookingOptionRepository;
import com.example.Repository.PartnerBookingRepository;
import com.example.Repository.VenueRepository;
import com.example.dto.Request.ConfirmBookingRequest;
import com.example.dto.Response.BookingInventoryResponse;
import com.example.dto.Response.PartnerBookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerBookingService {

    private static final String DEFAULT_CURRENCY = "VND";

    private final PartnerBookingRepository partnerBookingRepository;
    private final BookingHoldRepository bookingHoldRepository;
    private final BookingOptionRepository bookingOptionRepository;
    private final VenueRepository venueRepository;
    private final HoldInventoryService holdInventoryService;

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public PartnerBookingResponse confirmBooking(ConfirmBookingRequest request) {
        validateConfirmRequest(request);

        PartnerBooking idempotentBooking = partnerBookingRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .orElse(null);
        if (idempotentBooking != null) {
            ensureSameIdempotentRequest(idempotentBooking, request);
            return toResponse(idempotentBooking);
        }

        BookingHold hold = bookingHoldRepository.findByIdForUpdate(request.holdId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.HOLD_NOT_FOUND));

        // Kiểm tra lại sau khi đã khóa hold để tránh hai request đồng thời tạo hai booking.
        idempotentBooking = partnerBookingRepository
                .findByIdempotencyKey(request.idempotencyKey())
                .orElse(null);
        if (idempotentBooking != null) {
            ensureSameIdempotentRequest(idempotentBooking, request);
            return toResponse(idempotentBooking);
        }

        if (hold.getStatus() == BookingHoldStatus.HELD && isExpired(hold)) {
            holdInventoryService.restore(hold);
            hold.setStatus(BookingHoldStatus.EXPIRED);
            bookingHoldRepository.save(hold);
            throw new HoldExpiredException();
        }

        if (hold.getStatus() == BookingHoldStatus.EXPIRED) {
            throw new HoldExpiredException();
        }

        if (hold.getStatus() != BookingHoldStatus.HELD) {
            throw new AppException(PartnerErrorCode.HOLD_ALREADY_USED);
        }

        PartnerBooking booking = new PartnerBooking();
        booking.setHoldId(hold.getHoldId());
        booking.setCustomerRef(request.customerRef());
        booking.setQuantity(hold.getQuantity());
        booking.setUnitPrice(hold.getUnitPrice());
        booking.setTotalAmount(hold.getTotalAmount());
        booking.setCurrency(hold.getCurrency() == null ? DEFAULT_CURRENCY : hold.getCurrency());
        booking.setStatus(PartnerBookingStatus.CONFIRMED);
        booking.setIdempotencyKey(request.idempotencyKey());

        PartnerBooking savedBooking = partnerBookingRepository.save(booking);
        hold.setStatus(BookingHoldStatus.CONFIRMED);
        bookingHoldRepository.save(hold);

        return toResponse(savedBooking, hold);
    }

    @Transactional(readOnly = true)
    public PartnerBookingResponse getBooking(String bookingId) {
        PartnerBooking booking = findBooking(bookingId);
        return toResponse(booking);
    }

    @Transactional
    public PartnerBookingResponse cancelBooking(String bookingId) {
        validateId(bookingId);
        PartnerBooking booking = partnerBookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new AppException(PartnerErrorCode.BOOKING_NOT_FOUND));

        BookingHold hold = bookingHoldRepository.findByIdForUpdate(booking.getHoldId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.HOLD_NOT_FOUND));

        if (booking.getStatus() == PartnerBookingStatus.CANCELLED) {
            return toResponse(booking, hold);
        }

        if (booking.getStatus() == PartnerBookingStatus.COMPLETED) {
            throw new AppException(PartnerErrorCode.BOOKING_NOT_CANCELLABLE);
        }

        holdInventoryService.restore(hold);
        booking.setStatus(PartnerBookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        PartnerBooking savedBooking = partnerBookingRepository.save(booking);

        return toResponse(savedBooking, hold);
    }

    private PartnerBooking findBooking(String bookingId) {
        validateId(bookingId);
        return partnerBookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(PartnerErrorCode.BOOKING_NOT_FOUND));
    }

    private PartnerBookingResponse toResponse(PartnerBooking booking) {
        BookingHold hold = bookingHoldRepository.findById(booking.getHoldId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.HOLD_NOT_FOUND));
        return toResponse(booking, hold);
    }

    private PartnerBookingResponse toResponse(PartnerBooking booking, BookingHold hold) {
        BookingOption option = bookingOptionRepository.findById(hold.getOptionId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.OPTION_NOT_FOUND));
        Venue venue = venueRepository.findById(option.getVenueId())
                .orElseThrow(() -> new AppException(PartnerErrorCode.VENUE_NOT_FOUND));

        List<BookingInventoryResponse> inventoryResponses = holdInventoryService
                .loadForRead(hold)
                .stream()
                .sorted(Comparator
                        .comparing(
                                (HoldInventoryService.HeldInventory held) -> held.inventory().getServiceDate(),
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                held -> held.inventory().getStartTime(),
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                .map(held -> new BookingInventoryResponse(
                        held.inventory().getInventoryId(),
                        held.inventory().getServiceDate(),
                        held.inventory().getStartTime(),
                        held.inventory().getEndTime(),
                        held.quantity()
                ))
                .toList();

        return new PartnerBookingResponse(
                booking.getBookingId(),
                booking.getHoldId(),
                venue.getProviderId(),
                venue.getVenueId(),
                option.getOptionId(),
                venue.getServiceType(),
                booking.getCustomerRef(),
                booking.getQuantity(),
                booking.getUnitPrice(),
                booking.getTotalAmount(),
                booking.getCurrency() == null ? DEFAULT_CURRENCY : booking.getCurrency(),
                booking.getStatus(),
                inventoryResponses,
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }

    private void validateConfirmRequest(ConfirmBookingRequest request) {
        if (request == null
                || isBlank(request.holdId())
                || isBlank(request.customerRef())
                || isBlank(request.idempotencyKey())) {
            throw new AppException(PartnerErrorCode.INVALID_REQUEST);
        }
    }

    private void ensureSameIdempotentRequest(
            PartnerBooking booking,
            ConfirmBookingRequest request
    ) {
        if (!booking.getHoldId().equals(request.holdId())
                || !booking.getCustomerRef().equals(request.customerRef())) {
            throw new AppException(PartnerErrorCode.IDEMPOTENCY_CONFLICT);
        }
    }

    private boolean isExpired(BookingHold hold) {
        return hold.getExpiresAt() == null
                || !hold.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private void validateId(String id) {
        if (isBlank(id)) {
            throw new AppException(PartnerErrorCode.INVALID_REQUEST);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
