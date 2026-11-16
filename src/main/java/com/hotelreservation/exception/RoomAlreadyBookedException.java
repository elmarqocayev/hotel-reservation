package com.hotelreservation.exception;

import java.time.LocalDate;

public class RoomAlreadyBookedException extends RuntimeException {
    public RoomAlreadyBookedException(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        super("Room %d is already booked between %s and %s".formatted(roomId, checkIn, checkOut));
    }
}
