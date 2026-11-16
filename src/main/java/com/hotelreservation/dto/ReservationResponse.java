package com.hotelreservation.dto;

import com.hotelreservation.entity.Reservation;
import com.hotelreservation.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long roomId,
        String roomNumber,
        Long guestId,
        String guestFullName,
        String guestEmail,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getRoom().getId(),
                r.getRoom().getRoomNumber(),
                r.getGuest().getId(),
                r.getGuest().getFullName(),
                r.getGuest().getEmail(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getTotalPrice(),
                r.getStatus()
        );
    }
}
