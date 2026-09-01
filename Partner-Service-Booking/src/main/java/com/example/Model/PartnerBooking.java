package com.example.Model;

import com.example.Enum.PartnerBookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "partner_booking",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_partner_booking_hold", columnNames = "hold_id"),
                @UniqueConstraint(name = "uk_partner_booking_idempotency", columnNames = "idempotency_key")
        }
)
public class PartnerBooking {

    @Id
    @UuidGenerator
    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "hold_id")
    private String holdId;

    @Column(name = "customer_ref")
    private String customerRef;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PartnerBookingStatus status;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
