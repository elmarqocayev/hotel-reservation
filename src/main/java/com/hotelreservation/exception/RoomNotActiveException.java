package com.hotelreservation.exception;

public class RoomNotActiveException extends RuntimeException {
    public RoomNotActiveException(Long id) {
        super("Room " + id + " is not active and cannot be booked");
    }
}
