package com.moviereservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Entity representing a snack available for pre-order during the booking flow.
 * Snacks are managed by admins via the admin panel and displayed to users
 * in a popup before checkout. Only snacks marked as available are shown
 * to users — unavailable snacks remain in the database for future reactivation.
 */
@Entity
@Table(name = "snacks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Snack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the snack displayed in the booking popup and confirmation email */
    @Column(nullable = false)
    private String name;

    /** The price of the snack added to the reservation total */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Optional description shown in the snack selection popup */
    private String description;

    /** Emoji displayed alongside the snack name in the UI and confirmation email */
    private String emoji;

    /**
     * Whether this snack is currently available for pre-order.
     * When false the snack is hidden from the public booking flow
     * without being permanently deleted.
     * Defaults to true on creation.
     */
    private boolean available = true;

    /**
     * The serving size of the snack.
     * Displayed as a badge in the snack selection popup.
     * Defaults to MEDIUM on creation.
     */
    @Enumerated(EnumType.STRING)
    private SnackSize size = SnackSize.MEDIUM;

    /** Possible serving sizes for a snack */
    public enum SnackSize {
        SMALL,
        MEDIUM,
        LARGE
    }
}
