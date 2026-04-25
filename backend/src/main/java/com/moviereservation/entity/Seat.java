package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an individual seat in a cinema theatre.
 * Seats are automatically generated when a theatre is created,
 * based on the specified number of rows and seats per row.
 * Each seat belongs to a theatre and has a row label, seat number and type.
 */
@Entity
@Table(name = "seats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The theatre this seat belongs to.
     * Loaded lazily to avoid unnecessary joins when fetching seat lists.
     * JsonIgnoreProperties prevents circular serialization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnoreProperties({"seats", "showtimes", "hibernateLazyInitializer"})
    private Theater theater;

    /** Row identifier displayed on the seat map e.g. A, B, C */
    @Column(nullable = false)
    private String rowLabel;

    /** Seat number within the row e.g. 1, 2, 3 */
    @Column(nullable = false)
    private Integer seatNumber;

    /**
     * The type of seat determining pricing and visual display on the seat map.
     * STANDARD seats use the base ticket price.
     * VIP seats apply a $3.00 surcharge on top of the base ticket price.
     * WHEELCHAIR seats are defined in the data model for future accessibility
     * enhancements but are not yet implemented in the booking interface.
     * Defaults to STANDARD on creation.
     */
    @Enumerated(EnumType.STRING)
    private SeatType type = SeatType.STANDARD;

    /** Possible seat types */
    public enum SeatType {
        STANDARD,    // Regular seat at base ticket price
        VIP,         // Premium seat with $3.00 surcharge
        WHEELCHAIR   // Accessible seat — reserved for future implementation
    }
}