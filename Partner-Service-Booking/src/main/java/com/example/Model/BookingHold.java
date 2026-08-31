package com.example.Model;

import com.example.Enum.BookingHoldStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "booking_hold")
public class BookingHold {

    @Id
    @UuidGenerator
    @Column(name = "hold_id")
    private String holdId;

    @Column(name = "option_id")
    private String optionId;

    @Column(name = "inventory_id")
    private String inventoryId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingHoldStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
