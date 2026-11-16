package com.hotelreservation.service;

import com.hotelreservation.dto.GuestRequest;
import com.hotelreservation.dto.ReservationRequest;
import com.hotelreservation.dto.ReservationResponse;
import com.hotelreservation.entity.Guest;
import com.hotelreservation.entity.Reservation;
import com.hotelreservation.entity.ReservationStatus;
import com.hotelreservation.entity.Room;
import com.hotelreservation.exception.InvalidDateRangeException;
import com.hotelreservation.exception.ReservationNotFoundException;
import com.hotelreservation.exception.RoomAlreadyBookedException;
import com.hotelreservation.exception.RoomNotActiveException;
import com.hotelreservation.exception.RoomNotFoundException;
import com.hotelreservation.repository.GuestRepository;
import com.hotelreservation.repository.ReservationRepository;
import com.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;


    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        validateDateRange(checkIn, checkOut);


        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        if (!room.isActive()) {
            throw new RoomNotActiveException(room.getId());
        }


        boolean overlaps = reservationRepository.existsOverlappingReservation(
                room.getId(), ReservationStatus.CONFIRMED, checkIn, checkOut);
        if (overlaps) {
            throw new RoomAlreadyBookedException(room.getId(), checkIn, checkOut);
        }


        Guest guest = findOrCreateGuest(request.guest());

        BigDecimal totalPrice = calculateTotalPrice(room.getPricePerNight(), checkIn, checkOut);

        Reservation reservation = Reservation.builder()
                .room(room)
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .totalPrice(totalPrice)
                .status(ReservationStatus.CONFIRMED)
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(ReservationResponse::from)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }



    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isAfter(LocalDate.now())) {
            throw new InvalidDateRangeException("Check-in date must be in the future");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException("Check-out date must be after check-in date");
        }
    }

    private BigDecimal calculateTotalPrice(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private Guest findOrCreateGuest(GuestRequest request) {
        return guestRepository.findByEmail(request.email())
                .orElseGet(() -> guestRepository.save(Guest.builder()
                        .fullName(request.fullName())
                        .email(request.email())
                        .phone(request.phone())
                        .build()));
    }
}
