package com.hotelreservation.service;

import com.hotelreservation.dto.RoomRequest;
import com.hotelreservation.dto.RoomResponse;
import com.hotelreservation.entity.ReservationStatus;
import com.hotelreservation.entity.Room;
import com.hotelreservation.exception.DuplicateRoomNumberException;
import com.hotelreservation.exception.InvalidDateRangeException;
import com.hotelreservation.exception.RoomNotFoundException;
import com.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new DuplicateRoomNumberException(request.roomNumber());
        }

        Room room = Room.builder()
                .roomNumber(request.roomNumber())
                .type(request.type())
                .pricePerNight(request.pricePerNight())
                .active(request.active() == null || request.active())
                .build();

        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream().map(RoomResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        return roomRepository.findById(id)
                .map(RoomResponse::from)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException("Check-out date must be after check-in date");
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut, ReservationStatus.CONFIRMED)
                .stream()
                .map(RoomResponse::from)
                .toList();
    }
}
