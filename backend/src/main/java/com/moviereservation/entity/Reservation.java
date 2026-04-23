package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "password", "hibernateLazyInitializer"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    @JsonIgnoreProperties({"reservations", "hibernateLazyInitializer"})
    private Showtime showtime;

    @ManyToMany
    @JoinTable(
            name = "reservation_seats",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private List<Seat> seats;

    @ElementCollection
    @CollectionTable(name = "reservation_snacks", joinColumns = @JoinColumn(name = "reservation_id"))
    @MapKeyColumn(name = "snack_id")
    @Column(name = "quantity")
    private Map<Long, Integer> snacks = new HashMap<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.HELD;

    private String stripePaymentIntentId;
    private String stripeClientSecret;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    private LocalDateTime heldUntil;
    private static final int HOLD_DURATION_MINUTES = 15;

    @Column(unique = true)
    private String bookingReference;

    public void initializeHold() {
        this.heldUntil = LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(HOLD_DURATION_MINUTES);
        this.status = ReservationStatus.HELD;
    }

    public boolean isHoldExpired() {
        return heldUntil != null && LocalDateTime.now().isAfter(heldUntil);
    }

    public enum ReservationStatus {
        HELD, PENDING, CONFIRMED, CANCELLED, REFUNDED
    }
}
