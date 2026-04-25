package com.moviereservation.repository;

import com.moviereservation.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Showtime database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Includes a custom JPQL query to retrieve upcoming showtimes
 * for a specific movie, used for date filtering on the movie details page.
 */
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * Finds all showtimes for a specific movie regardless of date.
     * Used by the admin panel to display all scheduled showtimes
     * for management purposes.
     *
     * @param movieId the ID of the movie to retrieve showtimes for
     * @return a list of all showtimes for the given movie
     */
    List<Showtime> findByMovieId(Long movieId);

    /**
     * Finds all upcoming showtimes for a specific movie from a given date onwards.
     * Results are ordered by start time ascending so the earliest showtime
     * appears first. Used by the movie details page date filter to show
     * only future screenings to users.
     *
     * @param movieId the ID of the movie to retrieve showtimes for
     * @param from    the earliest start time to include — typically the start of the selected date
     * @return a list of upcoming showtimes ordered by start time
     */
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.movie.id = :movieId
        AND s.startTime >= :from
        ORDER BY s.startTime ASC
    """)
    List<Showtime> findUpcomingByMovieId(
            @Param("movieId") Long movieId,
            @Param("from") LocalDateTime from
    );
}