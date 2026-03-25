package com.autoservice.dto;

public class TourAvailabilityDto {
    private final Long tourId;
    private final int maxSeats;
    private final long bookedSeats;
    private final long availableSeats;
    private final boolean soldOut;

    public TourAvailabilityDto(Long tourId, int maxSeats, long bookedSeats) {
        this.tourId = tourId;
        this.maxSeats = maxSeats;
        this.bookedSeats = bookedSeats;
        this.availableSeats = Math.max(maxSeats - bookedSeats, 0L);
        this.soldOut = this.availableSeats == 0;
    }

    public Long getTourId() {
        return tourId;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public long getBookedSeats() {
        return bookedSeats;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public boolean isSoldOut() {
        return soldOut;
    }
}
