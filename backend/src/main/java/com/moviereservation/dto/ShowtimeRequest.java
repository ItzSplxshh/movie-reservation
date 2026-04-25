package com.moviereservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for showtime creation requests.
 * Contains all fields required to schedule a new screening.
 * The end time is automatically calculated by ShowtimeService
 * based on the movie's duration, so it is not included here.
 * All fields are mandatory and validated before reaching the controller.
 */
@Data
public class ShowtimeRequest {

    /** The ID of the movie being screened — must not be null */
    @NotNull
    private Long movieId;

    /** The ID of the theatre where the screening takes place — must not be null */
    @NotNull
    private Long theaterId;

    /** The date and time the screening starts — must not be null */
    @NotNull
    private LocalDateTime startTime;

    /** The ticket price for this screening — must not be null */
    @NotNull
    private BigDecimal ticketPrice;
}