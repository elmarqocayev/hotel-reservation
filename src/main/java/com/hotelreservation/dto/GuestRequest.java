package com.hotelreservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 characters")
        String phone
) {}
