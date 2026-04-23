package com.moviereservation.service;

import com.moviereservation.entity.Seat;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    @Cacheable(value = "seatAvailability", key = "#showtimeId")
    public List<Seat> getAllSeatsForShowtime(Long showtimeId) {
        Long theaterId = showtimeRepository.findById(showtimeId)
                .map(st -> st.getTheater().getId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        return seatRepository.findByTheaterId(theaterId);
    }

    @CacheEvict(value = "seatAvailability", allEntries = true)
    public void evictSeatCache() {
        // Called when reservations are created or cancelled
    }
}