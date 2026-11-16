package com.hotelreservation.dto;

import com.hotelreservation.entity.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank(message = "Room number is required")
        String roomNumber,

        @NotNull(message = "Room type is required")
        RoomType type,

        @NotNull(message = "Price per night is required")
        @DecimalMin(value = "0.01", message = "Price per night must be greater than 0")
        BigDecimal pricePerNight,

        Boolean active
) {}
