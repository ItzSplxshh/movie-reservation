package com.moviereservation.controller;

import com.moviereservation.entity.Seat;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import com.moviereservation.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatService seatService;

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<Seat>> getSeatsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(seatRepository.findByTheaterId(theaterId));
    }

    @GetMapping("/showtime/{showtimeId}/all")
    public ResponseEntity<List<Seat>> getAllSeatsForShowtime(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(seatService.getAllSeatsForShowtime(showtimeId));
    }
}