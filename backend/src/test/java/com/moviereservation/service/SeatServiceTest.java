package com.moviereservation.service;

import com.moviereservation.entity.Seat;
import com.moviereservation.entity.Showtime;
import com.moviereservation.entity.Theater;
import com.moviereservation.repository.SeatRepository;
import com.moviereservation.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.moviereservation.service.SeatService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private ShowtimeRepository showtimeRepository;

    private SeatService seatService;

    private Theater theater;
    private Showtime showtime;
    private Seat seat;

    @BeforeEach
    void setUp() {
        // Manually instantiate to bypass Spring caching proxy
        seatService = new SeatService(seatRepository, showtimeRepository);

        theater = Theater.builder()
                .id(1L)
                .name("CineVault Leicester")
                .totalRows(10)
                .seatsPerRow(12)
                .build();

        showtime = Showtime.builder()
                .id(1L)
                .theater(theater)
                .build();

        seat = Seat.builder()
                .id(1L)
                .theater(theater)
                .rowLabel("A")
                .seatNumber(1)
                .type(Seat.SeatType.STANDARD)
                .build();
    }

    @Test
    void getAllSeatsForShowtime_withValidShowtime_returnsSeats() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByTheaterId(1L)).thenReturn(List.of(seat));

        List<Seat> result = seatService.getAllSeatsForShowtime(1L);

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getRowLabel());
        assertEquals(1, result.get(0).getSeatNumber());
    }

    @Test
    void getAllSeatsForShowtime_withInvalidShowtime_throwsException() {
        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> seatService.getAllSeatsForShowtime(99L)
        );

        assertEquals("Showtime not found", exception.getMessage());
    }

    @Test
    void getAllSeatsForShowtime_returnsAllSeatTypes() {
        Seat vipSeat = Seat.builder()
                .id(2L)
                .theater(theater)
                .rowLabel("A")
                .seatNumber(2)
                .type(Seat.SeatType.VIP)
                .build();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByTheaterId(1L)).thenReturn(List.of(seat, vipSeat));

        List<Seat> result = seatService.getAllSeatsForShowtime(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getType() == Seat.SeatType.STANDARD));
        assertTrue(result.stream().anyMatch(s -> s.getType() == Seat.SeatType.VIP));
    }

    @Test
    void evictSeatCache_completesWithoutError() {
        assertDoesNotThrow(() -> seatService.evictSeatCache());
    }
}
