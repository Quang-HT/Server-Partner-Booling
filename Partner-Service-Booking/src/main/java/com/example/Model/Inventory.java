package com.example.Model;

import com.example.Enum.InventoryStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    @UuidGenerator
    @Column(name = "inventory_id")
    private String inventoryId;

    @Column(name = "option_id")
    private String optionId;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "available_quantity")
    private Integer availableQuantity;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    @Column(name = "version")
    private String version;
}
