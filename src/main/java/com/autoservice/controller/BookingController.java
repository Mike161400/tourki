package com.autoservice.controller;

import com.autoservice.domain.Booking;
import com.autoservice.repository.BookingRepository;
import com.autoservice.security.Role;
import com.autoservice.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final BusinessService businessService;

    public BookingController(BookingRepository bookingRepository, BusinessService businessService) {
        this.bookingRepository = bookingRepository;
        this.businessService = businessService;
    }

    @GetMapping
    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tour/{tourId}")
    public List<Booking> getByTour(@PathVariable Long tourId) {
        return bookingRepository.findByTourId(tourId);
    }

    @GetMapping("/me")
    public List<Booking> getMyBookings(Authentication authentication) {
        return bookingRepository.findByTravelerUsername(authentication.getName());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication authentication) {
        try {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()));
            return ResponseEntity.ok(businessService.cancelBooking(id, authentication.getName(), admin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
