package com.moviereservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReservationRequest {
    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<Long> seatIds;
}
