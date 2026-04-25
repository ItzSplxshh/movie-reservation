package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a scheduled film screening.
 * Links a movie to a theatre at a specific date and time with a ticket price.
 * Acts as the central booking entity — users select a showtime to begin
 * the seat selection and reservation process.
 */
@Entity
@Table(name = "showtimes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The film being screened.
     * Loaded lazily to avoid unnecessary joins.
     * JsonIgnoreProperties prevents circular serialization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnoreProperties({"showtimes", "hibernateLazyInitializer"})
    private Movie movie;

    /**
     * The theatre where the screening takes place.
     * Loaded lazily to avoid unnecessary joins.
     * JsonIgnoreProperties prevents circular serialization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonIgnoreProperties({"showtimes", "seats", "hibernateLazyInitializer"})
    private Theater theater;

    /** The date and time the screening starts */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * The date and time the screening ends.
     * Automatically calculated from startTime and movie duration
     * by ShowtimeService when a showtime is created.
     */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** The ticket price for this screening in USD */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal ticketPrice;

    /**
     * Current status of the showtime.
     * Defaults to SCHEDULED on creation.
     */
    @Enumerated(EnumType.STRING)
    private ShowtimeStatus status = ShowtimeStatus.SCHEDULED;

    /**
     * All reservations made for this showtime.
     * JsonIgnore prevents circular serialization.
     * Cascade ALL ensures reservations are deleted when a showtime is deleted.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    /** Possible statuses for a showtime */
    public enum ShowtimeStatus {
        SCHEDULED,   // Not yet started
        ONGOING,     // Currently screening
        COMPLETED,   // Screening has finished
        CANCELLED    // Showtime was cancelled
    }
}