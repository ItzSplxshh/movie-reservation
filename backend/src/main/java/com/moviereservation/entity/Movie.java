package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Entity representing a film in the CineVault system.
 * Stores all metadata about a movie including title, description,
 * cast, rating and current showing status.
 * A movie can have multiple showtimes across different theatres.
 */
@Entity
@Table(name = "movies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The title of the film — required field */
    @Column(nullable = false)
    private String title;

    /** Full synopsis of the film — stored as TEXT to support longer descriptions */
    @Column(columnDefinition = "TEXT")
    private String description;

    private String genre;

    /** Runtime of the film in minutes — used to calculate showtime end times */
    private Integer durationMinutes;

    private String director;
    private String cast;

    /** URL of the movie poster image displayed on the browse and details pages */
    private String posterUrl;

    private String trailerUrl;

    /** Average rating out of 10 */
    private Double rating;

    private String releaseYear;

    /**
     * Current showing status of the film.
     * Controls visibility on the public browsing page.
     * Defaults to NOW_SHOWING on creation.
     */
    @Enumerated(EnumType.STRING)
    private MovieStatus status = MovieStatus.NOW_SHOWING;

    /**
     * All showtimes scheduled for this movie.
     * JsonIgnore prevents circular serialization when returning movie data.
     * Cascade ALL ensures showtimes are deleted when a movie is deleted.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Showtime> showtimes;

    /** Possible showing statuses for a film */
    public enum MovieStatus {
        NOW_SHOWING,  // Currently screening in cinemas
        COMING_SOON,  // Scheduled for future release
        ENDED         // No longer screening
    }
}