package com.example.Model;

import com.example.Enum.ServiceType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "venue")
@Data
public class Venue {

    @Id
    @UuidGenerator
    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(name = "is_active")
    private Boolean isActive;
}
