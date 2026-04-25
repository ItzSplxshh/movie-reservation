package com.moviereservation.repository;

import com.moviereservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository interface for Seat database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Includes a custom JPQL query to retrieve available seats for a showtime,
 * which is the core query used for double booking prevention.
 */
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Finds all seats belonging to a specific theatre.
     * Used to retrieve the full seat layout for the seat map display
     * and by the Redis cache in SeatService.
     *
     * @param theaterId the ID of the theatre to retrieve seats for
     * @return a list of all seats in the given theatre
     */
    List<Seat> findByTheaterId(Long theaterId);

    /**
     * Counts the total number of seats in a theatre.
     * Used by the admin reports endpoint to calculate occupancy percentages.
     *
     * @param theaterId the ID of the theatre to count seats for
     * @return the total number of seats in the theatre
     */
    long countByTheaterId(Long theaterId);

    /**
     * Finds all available seats for a specific showtime.
     * Excludes seats that are already associated with a reservation
     * in HELD, PENDING or CONFIRMED status for the given showtime.
     * This query is the primary mechanism for preventing double booking,
     * as it is called before creating a reservation to verify seat availability.
     * Results are used by ReservationService to validate that all requested
     * seats are still available at the moment of booking.
     *
     * @param theaterId  the ID of the theatre to retrieve seats from
     * @param showtimeId the ID of the showtime to check availability for
     * @return a list of seats not currently held or booked for the given showtime
     */
    @Query("""
        SELECT s FROM Seat s
        WHERE s.theater.id = :theaterId
        AND s.id NOT IN (
            SELECT rs.id FROM Reservation r
            JOIN r.seats rs
            WHERE r.showtime.id = :showtimeId
            AND r.status IN ('HELD', 'PENDING', 'CONFIRMED')
        )
    """)
    List<Seat> findAvailableSeatsForShowtime(
            @Param("theaterId") Long theaterId,
            @Param("showtimeId") Long showtimeId
    );
}