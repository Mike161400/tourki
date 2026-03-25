package com.autoservice.repository;

import com.autoservice.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTourId(Long tourId);

    boolean existsByTourIdAndTravelerUsername(Long tourId, String travelerUsername);
}
