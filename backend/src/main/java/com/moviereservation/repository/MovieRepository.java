package com.moviereservation.repository;

import com.moviereservation.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(Movie.MovieStatus status);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
