package com.example.Repository;

import com.example.Enum.ServiceType;
import com.example.Model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue,String>, JpaSpecificationExecutor<Venue> {
    List<Venue> findAllByIsActive(Boolean isActive);
    List<Venue> findAllByServiceType(ServiceType serviceType);
    Optional<Venue> findByVenueIdAndIsActiveTrue(String venueId);
    List<Venue> findAllByServiceTypeAndIsActiveTrue(ServiceType serviceType);

}
