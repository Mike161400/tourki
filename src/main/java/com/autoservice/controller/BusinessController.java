package com.autoservice.controller;

import com.autoservice.dto.GuideWorkloadDto;
import com.autoservice.dto.ReviewRequest;
import com.autoservice.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/tours/{id}/auto-assign-guide")
    public ResponseEntity<?> autoAssignGuide(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(businessService.autoAssignGuide(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tours/{id}/book-seat")
    public ResponseEntity<?> bookSeat(@PathVariable Long id,
                                      @RequestParam(required = false) String travelerUsername,
                                      Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            if (admin && travelerUsername != null && !travelerUsername.isBlank()) {
                username = travelerUsername;
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(businessService.bookSeat(id, username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/tours/{id}/complete")
    public ResponseEntity<?> completeTour(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(businessService.completeTour(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tours/{id}/reviews")
    public ResponseEntity<?> addReview(@PathVariable Long id,
                                       @Valid @RequestBody ReviewRequest request,
                                       @RequestParam(required = false) String travelerUsername,
                                       Authentication authentication) {
        try {
            boolean admin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            String username = authentication.getName();
            if (admin && travelerUsername != null && !travelerUsername.isBlank()) {
                username = travelerUsername;
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(businessService.addReview(id, username, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/tours/{id}/availability")
    public ResponseEntity<?> getAvailability(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(businessService.getTourAvailability(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reports/guides/workload")
    public List<GuideWorkloadDto> getGuideWorkload() {
        return businessService.getGuideWorkload();
    }
}
