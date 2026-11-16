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

    /**
     * Creates a reservation:
     *  1. validates the date range
     *  2. locks the room row and verifies the room exists and is active
     *  3. rejects the request if the room is already booked in that period
     *  4. finds (or creates) the guest
     *  5. calculates the total price and saves the reservation as CONFIRMED
     */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        // 1. Date validation (business rules that Bean Validation cannot express)
        validateDateRange(checkIn, checkOut);

        // 2. The pessimistic lock makes concurrent bookings for the same room wait for
        //    each other, so the overlap check below is safe from race conditions.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        if (!room.isActive()) {
            throw new RoomNotActiveException(room.getId());
        }

        // 3. Overlap check. Only CONFIRMED reservations block a room; cancelled ones do not.
        //    Ranges are treated as [checkIn, checkOut): a guest may check in on the same
        //    day another guest checks out (same-day turnover is allowed).
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                room.getId(), ReservationStatus.CONFIRMED, checkIn, checkOut);
        if (overlaps) {
            throw new RoomAlreadyBookedException(room.getId(), checkIn, checkOut);
        }

        // 4. Reuse an existing guest by email, otherwise register a new one
        Guest guest = findOrCreateGuest(request.guest());

        // 5. Total price = number of nights * price per night
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

        // Cancelling an already cancelled reservation is harmless (idempotent)
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

    // ----------------------------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------------------------

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isAfter(LocalDate.now())) {
            throw new InvalidDateRangeException("Check-in date must be in the future");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException("Check-out date must be after check-in date");
        }
    }

    /** nights = checkOut - checkIn (in days); total = nights * pricePerNight */
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
