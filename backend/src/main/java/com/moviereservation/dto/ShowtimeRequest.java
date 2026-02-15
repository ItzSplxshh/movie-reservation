package com.moviereservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeRequest {
    @NotNull private Long movieId;
    @NotNull private Long theaterId;
    @NotNull private LocalDateTime startTime;
    @NotNull private BigDecimal ticketPrice;
}
