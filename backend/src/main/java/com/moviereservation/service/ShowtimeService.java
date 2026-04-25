package com.moviereservation.service;

import com.moviereservation.dto.ShowtimeRequest;
import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for showtime management.
 * Handles creating, retrieving and deleting cinema screenings.
 * Automatically calculates the end time from the movie duration
 * when a new showtime is created.
 */
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    /**
     * Returns all upcoming showtimes for a specific movie.
     * Only returns showtimes with a start time from the current moment onwards,
     * filtering out past screenings from the movie details page.
     * Results are ordered by start time ascending.
     *
     * @param movieId the ID of the movie to retrieve showtimes for
     * @return a list of upcoming showtimes for the given movie
     */
    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findUpcomingByMovieId(movieId, LocalDateTime.now());
    }

    /**
     * Returns a single showtime by its ID.
     * Used by the seat selection page and checkout page to display
     * showtime details including movie, theatre and start time.
     *
     * @param id the ID of the showtime to retrieve
     * @return the showtime with the given ID
     * @throws RuntimeException if no showtime exists with the given ID
     */
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime not found: " + id));
    }

    /**
     * Creates a new showtime linking a movie to a theatre.
     * Automatically calculates the end time by adding the movie's
     * duration in minutes to the provided start time, ensuring the
     * end time always reflects the actual runtime of the film.
     * The showtime is created with SCHEDULED status by default.
     *
     * @param req the showtime request containing movie ID, theatre ID, start time and ticket price
     * @return the created showtime with its generated ID
     * @throws RuntimeException if the movie or theatre does not exist
     */
    public Showtime createShowtime(ShowtimeRequest req) {
        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Theater theater = theaterRepository.findById(req.getTheaterId())
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .theater(theater)
                .startTime(req.getStartTime())
                // End time calculated automatically from movie duration
                .endTime(req.getStartTime().plusMinutes(movie.getDurationMinutes()))
                .ticketPrice(req.getTicketPrice())
                .status(Showtime.ShowtimeStatus.SCHEDULED)
                .build();

        return showtimeRepository.save(showtime);
    }

    /**
     * Deletes a showtime by its ID.
     * Due to Cascade ALL on the reservations relationship,
     * all associated reservations are also deleted.
     *
     * @param id the ID of the showtime to delete
     */
    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }
}