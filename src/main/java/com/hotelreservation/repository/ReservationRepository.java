package com.hotelreservation.repository;

import com.hotelreservation.entity.Reservation;
import com.hotelreservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Two date ranges overlap when each one starts before the other ends:
     *   existing.checkIn < requested.checkOut  AND  existing.checkOut > requested.checkIn
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status = :status
              AND r.checkInDate < :checkOut
              AND r.checkOutDate > :checkIn
            """)
    boolean existsOverlappingReservation(@Param("roomId") Long roomId,
                                         @Param("status") ReservationStatus status,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);
}
