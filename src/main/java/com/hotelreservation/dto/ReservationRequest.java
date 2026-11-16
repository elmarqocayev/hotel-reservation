package com.hotelreservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "Room id is required")
        Long roomId,

        @NotNull(message = "Guest details are required")
        @Valid
        GuestRequest guest,

        @NotNull(message = "Check-in date is required")
        @Future(message = "Check-in date must be in the future")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out date must be in the future")
        LocalDate checkOutDate
) {}
