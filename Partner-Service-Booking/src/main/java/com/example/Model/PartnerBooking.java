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
@Table(name = "partner_booking")
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
