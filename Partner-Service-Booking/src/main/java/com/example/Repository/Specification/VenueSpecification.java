package com.example.Repository.Specification;

import com.example.Enum.ServiceType;
import com.example.Model.Venue;
import org.springframework.data.jpa.domain.Specification;

public final class VenueSpecification {

    private VenueSpecification(){

    }

    public static Specification<Venue> isActive(){
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<Venue> serviceType(ServiceType serviceType){
        return (root, query, cb) ->
        {
            if(serviceType == null){
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("serviceType"),
                    serviceType
            );
        };
    }

    public static Specification<Venue> hasKeyword(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern =
                    "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("name")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("description")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("address")),
                            pattern
                    )
            );
        };
    }

}
