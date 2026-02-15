package com.moviereservation.controller;

import com.moviereservation.entity.Seat;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Seat>> getSeatsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(seatRepository.findByTheaterId(theaterId));
    }

    @GetMapping("/showtime/{showtimeId}/all")
    public ResponseEntity<List<Seat>> getAllSeatsForShowtime(@PathVariable Long showtimeId) {
        Long theaterId = showtimeRepository.findById(showtimeId)
                .map(st -> st.getTheater().getId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        return ResponseEntity.ok(seatRepository.findByTheaterId(theaterId));
    }
}
