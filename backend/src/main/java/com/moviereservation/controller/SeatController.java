package com.moviereservation.controller;

import com.moviereservation.entity.Seat;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import com.moviereservation.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for seat retrieval.
 * All endpoints are publicly accessible as seat availability
 * must be visible to unauthenticated users browsing showtimes.
 * Caching is handled at the service layer via SeatService
 * to avoid Spring Security proxy conflicts with controller-level caching.
 */
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatService seatService;

    /**
     * Returns all seats belonging to a specific theatre.
     * Used by the admin panel to display theatre seat layouts.
     *
     * @param theaterId the ID of the theatre to retrieve seats for
     * @return a list of all seats in the theatre
     */
    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Seat>> getSeatsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(seatRepository.findByTheaterId(theaterId));
    }

    /**
     * Returns all seats for the theatre associated with a given showtime.
     * Results are served from the Redis cache where available, falling back
     * to a database query on a cache miss.
     * Used by the seat map on the frontend to display all seats including
     * taken ones, which are then visually distinguished from available seats.
     *
     * @param showtimeId the ID of the showtime to retrieve seats for
     * @return a list of all seats in the showtime's theatre
     */
    @GetMapping("/showtime/{showtimeId}/all")
    public ResponseEntity<List<Seat>> getAllSeatsForShowtime(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(seatService.getAllSeatsForShowtime(showtimeId));
    }
}