package com.moviereservation.repository;

import com.moviereservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTheaterId(Long theaterId);

    @Query("""
        SELECT s FROM Seat s
        WHERE s.theater.id = :theaterId
        AND s.id NOT IN (
            SELECT rs.id FROM Reservation r
            JOIN r.seats rs
            WHERE r.showtime.id = :showtimeId
            AND r.status IN ('PENDING', 'CONFIRMED')
        )
    """)
    List<Seat> findAvailableSeatsForShowtime(
        @Param("theaterId") Long theaterId,
        @Param("showtimeId") Long showtimeId
    );
}
