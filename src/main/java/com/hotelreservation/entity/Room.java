package com.hotelreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
