package com.moviereservation;

import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Theater createTheater(Theater theater) {
        Theater saved = theaterRepository.save(theater);

        // Auto-generate seats (A1, A2 ... Jn)
        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < theater.getTotalRows(); row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int seatNum = 1; seatNum <= theater.getSeatsPerRow(); seatNum++) {
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
