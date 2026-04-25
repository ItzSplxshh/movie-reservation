package com.moviereservation.controller;

import com.moviereservation.dto.ShowtimeRequest;
import com.moviereservation.entity.Showtime;
import com.moviereservation.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for showtime management.
 * GET endpoints are publicly accessible allowing unauthenticated users
 * to browse available showtimes for each film.
 * POST and DELETE endpoints are restricted to ADMIN users only.
 */
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * Returns all showtimes for a specific movie.
     * Used by the movie details page to display available screening times
     * which the user can select to proceed to seat selection.
     *
     * @param movieId the ID of the movie to retrieve showtimes for
     * @return a list of all showtimes for the given movie
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Showtime>> getByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovie(movieId));
    }

    /**
     * Returns a single showtime by its ID.
     * Used by the seat selection and checkout pages to display
     * showtime details including movie title, theatre and start time.
     *
     * @param id the ID of the showtime to retrieve
     * @return the showtime with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Showtime> getById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * Creates a new showtime linking a movie to a theatre.
     * The end time is automatically calculated from the movie duration.
     * Restricted to ADMIN users only.
     *
     * @param req the showtime request containing movie ID, theatre ID, start time and ticket price
     * @return the created showtime
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Showtime> create(@Valid @RequestBody ShowtimeRequest req) {
        return ResponseEntity.ok(showtimeService.createShowtime(req));
    }

    /**
     * Deletes a showtime by ID.
     * Restricted to ADMIN users only.
     * Returns 204 No Content on successful deletion.
     *
     * @param id the ID of the showtime to delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }
}