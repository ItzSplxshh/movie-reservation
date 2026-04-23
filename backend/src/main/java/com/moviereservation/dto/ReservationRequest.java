package com.moviereservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReservationRequest {
    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<Long> seatIds;

    // snackId -> quantity
    private Map<Long, Integer> snacks;
}