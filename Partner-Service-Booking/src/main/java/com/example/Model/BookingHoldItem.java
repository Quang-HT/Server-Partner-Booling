package com.example.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Table(
        name = "booking_hold_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_hold_item_inventory",
                columnNames = {"hold_id", "inventory_id"}
        )
)
public class BookingHoldItem {

    @Id
    @UuidGenerator
    @Column(name = "hold_item_id")
    private String holdItemId;

    @Column(name = "hold_id", nullable = false)
    private String holdId;

    @Column(name = "inventory_id", nullable = false)
    private String inventoryId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
