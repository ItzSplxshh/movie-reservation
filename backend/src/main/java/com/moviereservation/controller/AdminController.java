package com.moviereservation.controller;

import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import com.moviereservation.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final TheaterRepository theaterRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TheaterService theaterService;

    @GetMapping("/theaters")
    public ResponseEntity<List<Theater>> getTheaters() {
        return ResponseEntity.ok(theaterRepository.findAll());
    }

    @PostMapping("/theaters")
    public ResponseEntity<Theater> createTheater(@RequestBody Theater theater) {
        return ResponseEntity.ok(theaterService.createTheater(theater));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<List<Showtime>> getShowtimes() {
        return ResponseEntity.ok(showtimeRepository.findAll());
    }
}
