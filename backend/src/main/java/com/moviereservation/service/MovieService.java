package com.moviereservation.service;

import com.moviereservation.entity.Movie;
import com.moviereservation.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getMoviesByStatus(Movie.MovieStatus status) {
        return movieRepository.findByStatus(status);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found: " + id));
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, Movie updated) {
        Movie movie = getMovieById(id);
        movie.setTitle(updated.getTitle());
        movie.setDescription(updated.getDescription());
        movie.setGenre(updated.getGenre());
        movie.setDurationMinutes(updated.getDurationMinutes());
        movie.setDirector(updated.getDirector());
        movie.setCast(updated.getCast());
        movie.setPosterUrl(updated.getPosterUrl());
        movie.setTrailerUrl(updated.getTrailerUrl());
        movie.setRating(updated.getRating());
        movie.setReleaseYear(updated.getReleaseYear());
        movie.setStatus(updated.getStatus());
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}
