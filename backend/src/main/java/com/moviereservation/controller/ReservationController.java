package com.moviereservation.controller;

import com.moviereservation.dto.ReservationRequest;
import com.moviereservation.entity.*;
import com.moviereservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing cinema seat reservations.
 * Handles seat availability queries, reservation creation and cancellation.
 * All endpoints except getAvailableSeats require a valid JWT token.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Returns a list of available seats for a given showtime.
     * Publicly accessible — no authentication required.
     */
    @GetMapping("/seats/{showtimeId}")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(reservationService.getAvailableSeats(showtimeId));
    }

    /**
     * Creates a new reservation for the authenticated user.
     * Validates seat availability, calculates total price including
     * VIP surcharges and snack costs, and initialises a 15-minute seat hold.
     * @param user the authenticated user extracted from the JWT token
     * @param req the reservation request containing showtime ID, seat IDs and snacks
     */
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.ok(reservationService.createReservation(user.getUsername(), req));
    }

    /**
     * Returns all reservations belonging to the authenticated user.
     * Includes reservations in all statuses: HELD, CONFIRMED, CANCELLED.
     */
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> myReservations(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.getUserReservations(user.getUsername()));
    }

    /**
     * Returns a specific reservation by ID.
     * Access is restricted to the reservation owner — throws an exception
     * if the authenticated user does not own the requested reservation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.getReservationById(id, user.getUsername()));
    }

    /**
     * Cancels a reservation by ID.
     * Sends a cancellation email if the reservation was previously confirmed.
     * Evicts the seat availability cache to reflect the freed seats.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, user.getUsername()));
    }
}