package com.moviereservation.service;

import com.moviereservation.dto.ShowtimeRequest;
import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findUpcomingByMovieId(movieId, LocalDateTime.now());
    }

    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime not found: " + id));
    }

    public Showtime createShowtime(ShowtimeRequest req) {
        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Theater theater = theaterRepository.findById(req.getTheaterId())
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .theater(theater)
                .startTime(req.getStartTime())
                .endTime(req.getStartTime().plusMinutes(movie.getDurationMinutes()))
                .ticketPrice(req.getTicketPrice())
                .status(Showtime.ShowtimeStatus.SCHEDULED)
                .build();

        return showtimeRepository.save(showtime);
    }

    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }
}
