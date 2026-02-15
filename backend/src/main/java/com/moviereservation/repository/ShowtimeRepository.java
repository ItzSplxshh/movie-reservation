package com.moviereservation.repository;

import com.moviereservation.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

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
