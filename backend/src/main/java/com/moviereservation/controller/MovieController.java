package com.moviereservation.controller;

import com.moviereservation.entity.Movie;
import com.moviereservation.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for movie management.
 * GET endpoints are publicly accessible allowing unauthenticated users
 * to browse the film catalogue. POST, PUT and DELETE endpoints are
 * restricted to ADMIN and SUPER_ADMIN roles.
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /**
     * Returns all movies or filters by status if a status parameter is provided.
     * Supports filtering by NOW_SHOWING, COMING_SOON or ENDED to allow the
     * frontend to display relevant films on different sections of the site.
     *
     * @param status optional filter — if provided, returns only movies with this status
     * @return a list of movies matching the filter or all movies if no filter is given
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam(required = false) Movie.MovieStatus status) {
        if (status != null) {
            return ResponseEntity.ok(movieService.getMoviesByStatus(status));
        }
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    /**
     * Returns a single movie by its ID.
     * Used by the movie details page to display full film information
     * and available showtimes.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /**
     * Creates a new movie in the system.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.createMovie(movie));
    }

    /**
     * Updates an existing movie by ID.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.updateMovie(id, movie));
    }

    /**
     * Deletes a movie by ID.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     * Returns 204 No Content on successful deletion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}