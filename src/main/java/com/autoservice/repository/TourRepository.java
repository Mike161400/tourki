package com.autoservice.repository;

import com.autoservice.domain.Tour;
import com.autoservice.domain.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByStatus(TourStatus status);

    List<Tour> findByGuideId(Long guideId);

    @Query("SELECT t FROM Tour t JOIN t.destinations d WHERE d.id = :destinationId")
    List<Tour> findByDestinationId(@Param("destinationId") Long destinationId);
}
