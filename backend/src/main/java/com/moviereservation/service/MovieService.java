package com.moviereservation.service;

import com.moviereservation.entity.Movie;
import com.moviereservation.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for movie management operations.
 * Provides CRUD functionality for the film catalogue,
 * used by both the public browsing interface and the admin panel.
 */
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    /**
     * Returns all movies in the system regardless of status.
     * Used by the admin panel to display the full film catalogue.
     *
     * @return a list of all movies
     */
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    /**
     * Returns all movies with a specific showing status.
     * Used by the public browsing page to filter films by
     * NOW_SHOWING, COMING_SOON or ENDED status.
     *
     * @param status the status to filter by
     * @return a list of movies with the given status
     */
    public List<Movie> getMoviesByStatus(Movie.MovieStatus status) {
        return movieRepository.findByStatus(status);
    }

    /**
     * Returns a single movie by its ID.
     * Used by the movie details page and showtime management.
     *
     * @param id the ID of the movie to retrieve
     * @return the movie with the given ID
     * @throws RuntimeException if no movie exists with the given ID
     */
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found: " + id));
    }

    /**
     * Creates a new movie in the system.
     * Called by the admin panel when adding a new film to the catalogue.
     *
     * @param movie the movie to create
     * @return the saved movie with its generated ID
     */
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    /**
     * Updates all fields of an existing movie.
     * Retrieves the existing movie first to ensure it exists,
     * then updates all fields with the provided values.
     *
     * @param id      the ID of the movie to update
     * @param updated the updated movie details
     * @return the saved movie with updated fields
     * @throws RuntimeException if no movie exists with the given ID
     */
    public Movie updateMovie(Long id, Movie updated) {
        Movie movie = getMovieById(id);
        movie.setTitle(updated.getTitle());
        movie.setDescription(updated.getDescription());
        movie.setGenre(updated.getGenre());
        movie.setDurationMinutes(updated.getDurationMinutes());
        movie.setDirector(updated.getDirector());
        movie.setCast(updated.getCast());
        movie.setPosterUrl(updated.getPosterUrl());
        movie.setTrailerUrl(updated.getTrailerUrl());
        movie.setRating(updated.getRating());
        movie.setReleaseYear(updated.getReleaseYear());
        movie.setStatus(updated.getStatus());
        return movieRepository.save(movie);
    }

    /**
     * Deletes a movie by its ID.
     * Due to Cascade ALL on the showtimes relationship,
     * all associated showtimes and their reservations are also deleted.
     *
     * @param id the ID of the movie to delete
     */
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}