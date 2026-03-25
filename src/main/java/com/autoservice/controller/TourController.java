package com.autoservice.controller;

import com.autoservice.domain.Destination;
import com.autoservice.domain.Guide;
import com.autoservice.domain.Tour;
import com.autoservice.domain.TourStatus;
import com.autoservice.dto.TourRequest;
import com.autoservice.repository.DestinationRepository;
import com.autoservice.repository.GuideRepository;
import com.autoservice.repository.TourRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourRepository tourRepository;
    private final GuideRepository guideRepository;
    private final DestinationRepository destinationRepository;

    public TourController(TourRepository tourRepository,
                          GuideRepository guideRepository,
                          DestinationRepository destinationRepository) {
        this.tourRepository = tourRepository;
        this.guideRepository = guideRepository;
        this.destinationRepository = destinationRepository;
    }

    @GetMapping
    public List<Tour> getAll() {
        return tourRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> getById(@PathVariable Long id) {
        return tourRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<Tour> getByStatus(@PathVariable TourStatus status) {
        return tourRepository.findByStatus(status);
    }

    @GetMapping("/guide/{guideId}")
    public List<Tour> getByGuide(@PathVariable Long guideId) {
        return tourRepository.findByGuideId(guideId);
    }

    @GetMapping("/destination/{destinationId}")
    public List<Tour> getByDestination(@PathVariable Long destinationId) {
        return tourRepository.findByDestinationId(destinationId);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TourRequest request) {
        try {
            Tour tour = buildTourFromRequest(new Tour(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(tourRepository.save(tour));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TourRequest request) {
        return tourRepository.findById(id).map(existing -> {
            try {
                Tour updated = buildTourFromRequest(existing, request);
                return ResponseEntity.ok(tourRepository.save(updated));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!tourRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tourRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Tour buildTourFromRequest(Tour tour, TourRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        Guide guide = null;
        if (request.getGuideId() != null) {
            guide = guideRepository.findById(request.getGuideId())
                    .orElseThrow(() -> new IllegalArgumentException("Guide not found: " + request.getGuideId()));

            if (!guide.isActive()) {
                throw new IllegalArgumentException("Guide is not active: " + request.getGuideId());
            }
        }

        List<Destination> destinations = new ArrayList<>();
        if (request.getDestinationIds() != null && !request.getDestinationIds().isEmpty()) {
            destinations = destinationRepository.findAllById(request.getDestinationIds());
            if (destinations.size() != request.getDestinationIds().size()) {
                throw new IllegalArgumentException("One or more destinations were not found");
            }
        }

        tour.setTitle(request.getTitle());
        tour.setDescription(request.getDescription());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());
        tour.setMaxSeats(request.getMaxSeats());
        tour.setPrice(request.getPrice());
        tour.setGuide(guide);
        tour.setDestinations(destinations);
        if (request.getStatus() != null) {
            tour.setStatus(request.getStatus());
        }
        return tour;
    }
}
