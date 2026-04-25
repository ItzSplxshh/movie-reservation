package com.moviereservation.service;

import com.moviereservation.entity.Seat;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for seat retrieval and cache management.
 * Implements a cache-aside strategy using Redis to reduce database load
 * for seat availability queries, which are the most frequently accessed
 * data in the system.
 */
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    /**
     * Retrieves all seats for a given showtime's theatre.
     * Results are cached in Redis with a 60-second TTL under the
     * "seatAvailability" cache, keyed by showtime ID.
     * On a cache hit, the database is bypassed entirely.
     * On a cache miss, the database is queried and the result stored in Redis.
     *
     * @param showtimeId the ID of the showtime to retrieve seats for
     * @return a list of all seats in the theatre for this showtime
     */
    @Cacheable(value = "seatAvailability", key = "#showtimeId")
    public List<Seat> getAllSeatsForShowtime(Long showtimeId) {
        Long theaterId = showtimeRepository.findById(showtimeId)
                .map(st -> st.getTheater().getId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        return seatRepository.findByTheaterId(theaterId);
    }

    /**
     * Evicts all entries from the seat availability cache.
     * Called whenever a reservation is created or cancelled to ensure
     * subsequent requests reflect the latest seat availability from the database.
     * Uses allEntries = true as reservations can affect any showtime's availability.
     */
    @CacheEvict(value = "seatAvailability", allEntries = true)
    public void evictSeatCache() {
        // Cache eviction is handled entirely by the @CacheEvict annotation
    }
}