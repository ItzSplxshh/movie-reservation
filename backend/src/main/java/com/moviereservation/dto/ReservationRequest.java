package com.moviereservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for reservation creation requests.
 * Contains the showtime, selected seats and optional snack pre-orders
 * required to create a new reservation with a 15-minute seat hold.
 * Validated using Bean Validation annotations before reaching the controller.
 */
@Data
public class ReservationRequest {

    /** The ID of the showtime being booked — must not be null */
    @NotNull
    private Long showtimeId;

    /** The IDs of the seats selected by the user — must contain at least one seat */
    @NotEmpty
    private List<Long> seatIds;

    /**
     * Optional snack pre-orders for this reservation.
     * Maps snack ID to the quantity ordered.
     * Snack costs are added to the reservation total price.
     * Null or empty if no snacks were selected.
     */
    private Map<Long, Integer> snacks;
}