package com.moviereservation.service;

import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for theatre management.
 * Handles theatre creation with automatic seat generation.
 * When a new theatre is created, individual seat records are
 * generated for every position based on the specified number
 * of rows and seats per row, ensuring the seat map is immediately
 * available for showtime scheduling without manual seat entry.
 */
@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;

    /**
     * Creates a new theatre and automatically generates all seat records.
     * Seats are labelled using alphabetical row labels (A, B, C...) and
     * sequential seat numbers within each row (1, 2, 3...).
     * The first row (row A) is automatically assigned VIP type with a
     * $3.00 surcharge applied at booking time. All other rows are STANDARD.
     * Wrapped in a transaction to ensure the theatre and all its seats
     * are saved atomically — if seat generation fails, the theatre is
     * also rolled back.
     *
     * @param theater the theatre to create with totalRows and seatsPerRow defined
     * @return the saved theatre with its generated ID
     */
    @Transactional
    public Theater createTheater(Theater theater) {
        Theater saved = theaterRepository.save(theater);

        // Generate seat records for every position in the theatre
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < theater.getTotalRows(); row++) {
            // Convert row index to alphabetical label e.g. 0=A, 1=B, 2=C
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int seatNum = 1; seatNum <= theater.getSeatsPerRow(); seatNum++) {
                // Row A is automatically designated as VIP
                Seat.SeatType type = (row == 0) ? Seat.SeatType.VIP : Seat.SeatType.STANDARD;
                seats.add(Seat.builder()
                        .theater(saved)
                        .rowLabel(rowLabel)
                        .seatNumber(seatNum)
                        .type(type)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
        return saved;
    }
}