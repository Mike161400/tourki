package com.autoservice.repository;

import com.autoservice.domain.Booking;
import com.autoservice.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByTourId(Long tourId);

    List<Booking> findByTravelerUsername(String travelerUsername);

    boolean existsByTourIdAndTravelerUsernameAndStatus(Long tourId, String travelerUsername, BookingStatus status);

    long countByTourIdAndStatus(Long tourId, BookingStatus status);
}
