package com.example.Enum;

import com.example.Exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum PartnerErrorCode implements BaseErrorCode {


    INVALID_REQUEST(
            "PARTNER_400_001",
            "Invalid request",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_DATE_RANGE(
            "PARTNER_400_002",
            "Invalid date range",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_TIME_RANGE(
            "PARTNER_400_003",
            "Invalid time range",
            HttpStatus.BAD_REQUEST
    ),

    VENUE_SERVICE_TYPE_MISMATCH(
            "PARTNER_400_004",
            "Venue does not support the requested service type",
            HttpStatus.BAD_REQUEST
    ),

    VENUE_NOT_FOUND(
            "PARTNER_404_001",
            "Venue not found",
            HttpStatus.NOT_FOUND
    ),

    OPTION_NOT_FOUND(
            "PARTNER_404_002",
            "Booking option not found",
            HttpStatus.NOT_FOUND
    ),

    INVENTORY_NOT_FOUND(
            "PARTNER_404_003",
            "Inventory not found",
            HttpStatus.NOT_FOUND
    ),

    OPTION_UNAVAILABLE(
            "PARTNER_409_001",
            "Booking option is unavailable",
            HttpStatus.CONFLICT
    ),

    HOLD_NOT_FOUND(
            "PARTNER_404_004",
            "Hold not found",
            HttpStatus.NOT_FOUND
    ),

    HOLD_EXPIRED(
            "PARTNER_410_001",
            "Hold has expired",
            HttpStatus.GONE
    ),

    HOLD_ALREADY_USED(
            "PARTNER_409_002",
            "Hold has already been used",
            HttpStatus.CONFLICT
    ),

    BOOKING_NOT_FOUND(
            "PARTNER_404_005",
            "Booking not found",
            HttpStatus.NOT_FOUND
    ),

    BOOKING_NOT_CANCELLABLE(
            "PARTNER_409_004",
            "Booking cannot be cancelled in its current status",
            HttpStatus.CONFLICT
    ),

    IDEMPOTENCY_CONFLICT(
            "PARTNER_409_003",
            "Idempotency key conflict",
            HttpStatus.CONFLICT
    ),

    INTERNAL_ERROR(
            "PARTNER_500_001",
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;


    @Override
    public HttpStatusCode getStatusCode() {
        return httpStatus;
    }
}
