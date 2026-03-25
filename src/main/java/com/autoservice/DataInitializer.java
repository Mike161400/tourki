package com.autoservice;

import com.autoservice.domain.*;
import com.autoservice.repository.*;
import com.autoservice.security.AppUser;
import com.autoservice.security.AppUserRepository;
import com.autoservice.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DestinationRepository destinationRepository;
    private final GuideRepository guideRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DestinationRepository destinationRepository,
                           GuideRepository guideRepository,
                           TourRepository tourRepository,
                           BookingRepository bookingRepository,
                           ReviewRepository reviewRepository,
                           AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder) {
        this.destinationRepository = destinationRepository;
        this.guideRepository = guideRepository;
        this.tourRepository = tourRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfAbsent("admin", "Admin1234!", Role.ROLE_ADMIN);
        createUserIfAbsent("guide1", "Guide1234!", Role.ROLE_GUIDE);
        createUserIfAbsent("guide2", "Guide2234!", Role.ROLE_GUIDE);
        createUserIfAbsent("traveler1", "Traveler1234!", Role.ROLE_TRAVELER);
        createUserIfAbsent("traveler2", "Traveler2234!", Role.ROLE_TRAVELER);

        if (tourRepository.count() > 0) {
            log.info("Travel data already seeded, skipping domain initialization.");
            return;
        }

        log.info("Seeding database with initial travel data...");

        Destination baikal = createDestination("Lake Baikal", "Russia", "Irkutsk", "Ice trek and Siberian nature.");
        Destination elbrus = createDestination("Elbrus", "Russia", "Kabardino-Balkaria", "Mountain climbing and alpine trails.");
        Destination dagestan = createDestination("Dagestan Canyons", "Russia", "Makhachkala", "Canyon routes and ethnic villages.");
        Destination altai = createDestination("Altai", "Russia", "Gorno-Altaysk", "Trekking, rivers, and mountain lakes.");
        Destination karelia = createDestination("Karelia", "Russia", "Petrozavodsk", "Kayaks, forests, and northern architecture.");

        Guide guideA = createGuide("Olga Petrova", "olga.petrova@travel.local", "RU,EN", 6, true);
        Guide guideB = createGuide("Nikolay Sidorov", "n.sidorov@travel.local", "RU,EN,DE", 9, true);
        Guide guideC = createGuide("Maksim Ivanov", "m.ivanov@travel.local", "RU", 4, true);

        LocalDate today = LocalDate.now();

        Tour completedTour = createTour(
                "Winter Baikal Expedition",
                "Seven-day route across frozen lake and coastal villages.",
                today.minusDays(12),
                today.minusDays(5),
                12,
                new BigDecimal("89000.00"),
                TourStatus.COMPLETED,
                guideA,
                List.of(baikal)
        );

        Tour openTour = createTour(
                "Spring Altai Trek",
                "Hiking route with camp nights and glacier viewpoints.",
                today.plusDays(20),
                today.plusDays(28),
                10,
                new BigDecimal("76000.00"),
                TourStatus.OPEN_FOR_BOOKING,
                guideB,
                List.of(altai)
        );

        Tour inProgressTour = createTour(
                "Dagestan Canyon Discovery",
                "Regional culture, canyon viewpoints, and jeep transfer.",
                today.minusDays(1),
                today.plusDays(4),
                14,
                new BigDecimal("54000.00"),
                TourStatus.IN_PROGRESS,
                guideC,
                List.of(dagestan)
        );

        Tour soldOutTour = createTour(
                "Elbrus Base Camp",
                "Acclimatization route with technical mountain training.",
                today.plusDays(35),
                today.plusDays(44),
                2,
                new BigDecimal("99000.00"),
                TourStatus.OPEN_FOR_BOOKING,
                guideB,
                List.of(elbrus, karelia)
        );

        createBooking(completedTour, "traveler1", BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(20), null);
        createBooking(completedTour, "traveler2", BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(19), null);

        createBooking(openTour, "traveler1", BookingStatus.CONFIRMED, LocalDateTime.now().minusDays(1), null);
        createBooking(openTour, "traveler2", BookingStatus.CANCELLED, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        createBooking(soldOutTour, "traveler1", BookingStatus.CONFIRMED, LocalDateTime.now().minusHours(12), null);
        createBooking(soldOutTour, "traveler2", BookingStatus.CONFIRMED, LocalDateTime.now().minusHours(10), null);

        createReview(completedTour, "traveler1", 5, "Excellent logistics and guide support.");

        log.info("Seed complete: {} destinations, {} guides, {} tours, {} bookings, {} reviews.",
                destinationRepository.count(),
                guideRepository.count(),
                tourRepository.count(),
                bookingRepository.count(),
                reviewRepository.count());
    }

    private void createUserIfAbsent(String username, String rawPassword, Role role) {
        if (!appUserRepository.existsByUsername(username)) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            appUserRepository.save(user);
        }
    }

    private Destination createDestination(String name, String country, String city, String description) {
        Destination destination = new Destination();
        destination.setName(name);
        destination.setCountry(country);
        destination.setCity(city);
        destination.setDescription(description);
        return destinationRepository.save(destination);
    }

    private Guide createGuide(String fullName, String email, String languages, int experienceYears, boolean active) {
        Guide guide = new Guide();
        guide.setFullName(fullName);
        guide.setEmail(email);
        guide.setLanguages(languages);
        guide.setExperienceYears(experienceYears);
        guide.setActive(active);
        return guideRepository.save(guide);
    }

    private Tour createTour(String title,
                            String description,
                            LocalDate startDate,
                            LocalDate endDate,
                            int maxSeats,
                            BigDecimal price,
                            TourStatus status,
                            Guide guide,
                            List<Destination> destinations) {
        Tour tour = new Tour();
        tour.setTitle(title);
        tour.setDescription(description);
        tour.setStartDate(startDate);
        tour.setEndDate(endDate);
        tour.setMaxSeats(maxSeats);
        tour.setPrice(price);
        tour.setStatus(status);
        tour.setGuide(guide);
        tour.setDestinations(destinations);
        return tourRepository.save(tour);
    }

    private Booking createBooking(Tour tour,
                                  String travelerUsername,
                                  BookingStatus status,
                                  LocalDateTime bookedAt,
                                  LocalDateTime cancelledAt) {
        Booking booking = new Booking();
        booking.setTour(tour);
        booking.setTravelerUsername(travelerUsername);
        booking.setStatus(status);
        booking.setBookedAt(bookedAt);
        booking.setCancelledAt(cancelledAt);
        return bookingRepository.save(booking);
    }

    private Review createReview(Tour tour, String travelerUsername, int rating, String comment) {
        Review review = new Review();
        review.setTour(tour);
        review.setTravelerUsername(travelerUsername);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }
}
