package com.hotelreservation.repository;

import com.hotelreservation.entity.ReservationStatus;
import com.hotelreservation.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomNumber(String roomNumber);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);


    @Query("""
            SELECT r FROM Room r
            WHERE r.active = true
              AND r.id NOT IN (
                  SELECT res.room.id FROM Reservation res
                  WHERE res.status = :status
                    AND res.checkInDate < :checkOut
                    AND res.checkOutDate > :checkIn
              )
            ORDER BY r.roomNumber
            """)
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("status") ReservationStatus status);
}
