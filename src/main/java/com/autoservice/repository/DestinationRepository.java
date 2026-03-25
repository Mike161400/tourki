package com.autoservice.repository;

import com.autoservice.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    boolean existsByNameAndCityAndCountry(String name, String city, String country);
}
