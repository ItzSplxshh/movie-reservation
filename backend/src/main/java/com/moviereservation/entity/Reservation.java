package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a cinema seat reservation.
 * Tracks the full lifecycle of a booking from initial seat selection
 * through payment confirmation, including seat hold expiry management,
 * Stripe payment integration and snack pre-orders.
 */
@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who made this reservation — loaded lazily to avoid unnecessary joins */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "password", "hibernateLazyInitializer"})
    private User user;

    /** The showtime this reservation is for — loaded lazily */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "hibernateLazyInitializer"})
    private Showtime showtime;

    /**
     * The seats selected for this reservation.
     * Stored in the reservation_seats join table to support
     * multiple seats per reservation.
     */
    @ManyToMany
    @JoinTable(
            name = "reservation_seats",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private List<Seat> seats;

    /**
     * Snacks pre-ordered with this reservation.
     * Stored as a map of snack ID to quantity in the reservation_snacks table.
     */
    @ElementCollection
    @CollectionTable(name = "reservation_snacks", joinColumns = @JoinColumn(name = "reservation_id"))
    @MapKeyColumn(name = "snack_id")
    @Column(name = "quantity")
    private Map<Long, Integer> snacks = new HashMap<>();

    /** Total price including seat tickets, VIP surcharges and snack costs */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Current status of the reservation.
     * HELD — seats reserved, awaiting payment
     * PENDING — payment initiated
     * CONFIRMED — payment successful
     * CANCELLED — reservation cancelled or hold expired
     * REFUNDED — payment refunded
     */
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.HELD;

    /** Stripe PaymentIntent ID linking this reservation to a Stripe payment */
    private String stripePaymentIntentId;

    /** Stripe client secret returned to the frontend to complete payment */
    private String stripeClientSecret;

    /** Timestamp when this reservation was created — immutable after creation */
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Timestamp when payment was confirmed — set by the Stripe webhook handler */
    private LocalDateTime paidAt;

    /** UTC timestamp when the seat hold expires — null after payment confirmation */
    private LocalDateTime heldUntil;

    /** Duration of the seat hold in minutes */
    private static final int HOLD_DURATION_MINUTES = 15;

    /**
     * Unique booking reference generated on payment confirmation.
     * Format: CV-YEAR-XXXXX (e.g. CV-2026-A3X9K)
     */
    @Column(unique = true)
    private String bookingReference;

    /**
     * Initialises the 15-minute seat hold timer.
     * Uses UTC to avoid timezone inconsistencies across different server environments.
     * Called when a reservation is first created before payment is initiated.
     */
    public void initializeHold() {
        this.heldUntil = LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(HOLD_DURATION_MINUTES);
        this.status = ReservationStatus.HELD;
    }

    /**
     * Checks whether the seat hold has expired.
     * Called by the scheduled background job to identify reservations
     * that should be automatically cancelled and their seats released.
     */
    public boolean isHoldExpired() {
        return heldUntil != null && LocalDateTime.now().isAfter(heldUntil);
    }

    /** Possible statuses representing the full reservation lifecycle */
    public enum ReservationStatus {
        HELD, PENDING, CONFIRMED, CANCELLED, REFUNDED
    }
}