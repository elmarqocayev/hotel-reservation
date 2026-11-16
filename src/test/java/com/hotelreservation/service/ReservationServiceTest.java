package com.hotelreservation.service;

import com.hotelreservation.dto.GuestRequest;
import com.hotelreservation.dto.ReservationRequest;
import com.hotelreservation.dto.ReservationResponse;
import com.hotelreservation.entity.Guest;
import com.hotelreservation.entity.Reservation;
import com.hotelreservation.entity.Room;
import com.hotelreservation.entity.RoomType;
import com.hotelreservation.exception.InvalidDateRangeException;
import com.hotelreservation.exception.RoomAlreadyBookedException;
import com.hotelreservation.repository.GuestRepository;
import com.hotelreservation.repository.ReservationRepository;
import com.hotelreservation.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private GuestRepository guestRepository;

    @InjectMocks private ReservationService reservationService;

    private Room room;
    private final GuestRequest guest = new GuestRequest("Aysel Mammadova", "aysel@example.com", "+994501234567");

    @BeforeEach
    void setUp() {
        room = Room.builder().id(1L).roomNumber("101").type(RoomType.DOUBLE)
                .pricePerNight(new BigDecimal("100.00")).active(true).build();
    }

    @Test
    void createReservation_calculatesTotalPriceFromNights() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(false);
        when(guestRepository.findByEmail(guest.email())).thenReturn(Optional.empty());
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.createReservation(
                new ReservationRequest(1L, guest, checkIn, checkOut));

        assertThat(response.totalPrice()).isEqualByComparingTo("300.00");
    }

    @Test
    void createReservation_throwsWhenRoomAlreadyBooked() {
        LocalDate checkIn = LocalDate.now().plusDays(10);

        when(roomRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.existsOverlappingReservation(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(1L, guest, checkIn, checkIn.plusDays(2))))
                .isInstanceOf(RoomAlreadyBookedException.class);
    }

    @Test
    void createReservation_throwsWhenCheckOutIsNotAfterCheckIn() {
        LocalDate checkIn = LocalDate.now().plusDays(10);

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(1L, guest, checkIn, checkIn)))
                .isInstanceOf(InvalidDateRangeException.class);
    }
}
