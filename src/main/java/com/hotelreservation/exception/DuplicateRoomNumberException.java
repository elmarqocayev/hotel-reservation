package com.hotelreservation.exception;

public class DuplicateRoomNumberException extends RuntimeException {
    public DuplicateRoomNumberException(String roomNumber) {
        super("Room with number " + roomNumber + " already exists");
    }
}
