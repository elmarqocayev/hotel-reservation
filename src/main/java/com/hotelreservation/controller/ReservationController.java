package com.hotelreservation.controller;

import com.hotelreservation.dto.ReservationRequest;
import com.hotelreservation.dto.ReservationResponse;
import com.hotelreservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody ReservationRequest request) {
        return reservationService.createReservation(request);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }
}
