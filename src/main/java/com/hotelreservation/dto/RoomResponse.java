package com.hotelreservation.dto;

import com.hotelreservation.entity.Room;
import com.hotelreservation.entity.RoomType;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        BigDecimal pricePerNight,
        boolean active
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getType(),
                room.getPricePerNight(), room.isActive());
    }
}
