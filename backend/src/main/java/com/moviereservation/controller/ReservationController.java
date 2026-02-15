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

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/seats/{showtimeId}")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(reservationService.getAvailableSeats(showtimeId));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.ok(reservationService.createReservation(user.getUsername(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> myReservations(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.getUserReservations(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.getReservationById(id, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, user.getUsername()));
    }
}
