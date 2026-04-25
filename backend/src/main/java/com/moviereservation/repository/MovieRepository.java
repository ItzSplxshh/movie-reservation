package com.moviereservation.repository;

import com.moviereservation.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Movie database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Custom query methods are automatically implemented by Spring Data JPA
 * based on method naming conventions.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Finds all movies with a specific showing status.
     * Used to filter movies by NOW_SHOWING, COMING_SOON or ENDED
     * on the public browsing page.
     *
     * @param status the status to filter by
     * @return a list of movies with the given status
     */
    List<Movie> findByStatus(Movie.MovieStatus status);

    /**
     * Finds all movies whose title contains the given string, case-insensitive.
     * Used by the search functionality on the browse films page.
     *
     * @param title the search term to match against movie titles
     * @return a list of movies whose title contains the search term
     */
    List<Movie> findByTitleContainingIgnoreCase(String title);
}