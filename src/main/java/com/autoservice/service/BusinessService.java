package com.autoservice.service;

import com.autoservice.domain.*;
import com.autoservice.dto.GuideWorkloadDto;
import com.autoservice.dto.ReviewRequest;
import com.autoservice.dto.TourAvailabilityDto;
import com.autoservice.repository.BookingRepository;
import com.autoservice.repository.GuideRepository;
import com.autoservice.repository.ReviewRepository;
import com.autoservice.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessService {

    private final TourRepository tourRepository;
    private final GuideRepository guideRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public BusinessService(TourRepository tourRepository,
                           GuideRepository guideRepository,
                           BookingRepository bookingRepository,
                           ReviewRepository reviewRepository) {
        this.tourRepository = tourRepository;
        this.guideRepository = guideRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Tour autoAssignGuide(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));

        if (tour.getStatus() == TourStatus.CANCELLED || tour.getStatus() == TourStatus.COMPLETED) {
            throw new IllegalStateException("Cannot assign guide to tour in status: " + tour.getStatus());
        }

        List<Guide> guides = guideRepository.findActiveGuidesByWorkloadAsc();
        if (guides.isEmpty()) {
            throw new IllegalStateException("No active guides available");
        }

        tour.setGuide(guides.get(0));
        return tourRepository.save(tour);
    }

    @Transactional
    public Booking bookSeat(Long tourId, String travelerUsername) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));

        if (tour.getStatus() != TourStatus.OPEN_FOR_BOOKING) {
            throw new IllegalStateException("Tour is not open for booking. Current status: " + tour.getStatus());
        }

        if (tour.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot book a tour that has already started");
        }

        if (bookingRepository.existsByTourIdAndTravelerUsernameAndStatus(tourId, travelerUsername, BookingStatus.CONFIRMED)) {
            throw new IllegalStateException("Traveler already has an active booking for this tour");
        }

        long confirmedSeats = bookingRepository.countByTourIdAndStatus(tourId, BookingStatus.CONFIRMED);
        if (confirmedSeats >= tour.getMaxSeats()) {
            throw new IllegalStateException("No seats available for this tour");
        }

        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setTravelerUsername(travelerUsername);
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String requesterUsername, boolean admin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!admin && !booking.getTravelerUsername().equals(requesterUsername)) {
            throw new IllegalStateException("You can cancel only your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (!booking.getTour().getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel booking for started or completed tour");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Transactional
    public Tour completeTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));

        if (tour.getStatus() == TourStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled tour cannot be completed");
        }

        if (tour.getStatus() == TourStatus.COMPLETED) {
            return tour;
        }

        if (tour.getEndDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Tour can be completed only on or after end date");
        }

        tour.setStatus(TourStatus.COMPLETED);
        return tourRepository.save(tour);
    }

    @Transactional
    public Review addReview(Long tourId, String travelerUsername, ReviewRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));

        if (tour.getStatus() != TourStatus.COMPLETED) {
            throw new IllegalStateException("Review is allowed only for completed tours");
        }

        boolean wasParticipant = bookingRepository.existsByTourIdAndTravelerUsernameAndStatus(
                tourId,
                travelerUsername,
                BookingStatus.CONFIRMED
        );
        if (!wasParticipant) {
            throw new IllegalStateException("Only participants of this completed tour can leave a review");
        }

        if (reviewRepository.existsByTourIdAndTravelerUsername(tourId, travelerUsername)) {
            throw new IllegalStateException("Review already exists for this traveler and tour");
        }

        Review review = new Review();
        review.setTour(tour);
        review.setTravelerUsername(travelerUsername);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public TourAvailabilityDto getTourAvailability(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));

        long confirmedSeats = bookingRepository.countByTourIdAndStatus(tourId, BookingStatus.CONFIRMED);
        return new TourAvailabilityDto(tourId, tour.getMaxSeats(), confirmedSeats);
    }

    @Transactional(readOnly = true)
    public List<GuideWorkloadDto> getGuideWorkload() {
        List<Guide> guides = guideRepository.findAll();
        List<Tour> allTours = tourRepository.findAll();

        return guides.stream().map(guide -> {
            List<Tour> guideTours = allTours.stream()
                    .filter(t -> t.getGuide() != null && t.getGuide().getId().equals(guide.getId()))
                    .collect(Collectors.toList());

            long openTours = guideTours.stream().filter(t -> t.getStatus() == TourStatus.OPEN_FOR_BOOKING).count();
            long inProgressTours = guideTours.stream().filter(t -> t.getStatus() == TourStatus.IN_PROGRESS).count();
            long completedTours = guideTours.stream().filter(t -> t.getStatus() == TourStatus.COMPLETED).count();

            return new GuideWorkloadDto(
                    guide.getId(),
                    guide.getFullName(),
                    openTours,
                    inProgressTours,
                    completedTours,
                    guideTours.size()
            );
        }).collect(Collectors.toList());
    }
}
