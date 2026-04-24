package com.moviereservation.service;

import com.moviereservation.dto.ReservationRequest;
import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingConfirmationService bookingConfirmationService;
    @Mock private SeatService seatService;
    @Mock private SnackRepository snackRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Showtime showtime;
    private Theater theater;
    private Seat seat;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        theater = Theater.builder()
                .id(1L)
                .name("CineVault Leicester")
                .totalRows(10)
                .seatsPerRow(12)
                .build();

        showtime = Showtime.builder()
                .id(1L)
                .theater(theater)
                .ticketPrice(new BigDecimal("7.00"))
                .build();

        user = User.builder()
                .email("test@cinevault.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        seat = Seat.builder()
                .id(1L)
                .theater(theater)
                .rowLabel("A")
                .seatNumber(1)
                .type(Seat.SeatType.STANDARD)
                .build();

        request = new ReservationRequest();
        request.setShowtimeId(1L);
        request.setSeatIds(List.of(1L));
    }

    // ── Create Reservation Tests ──────────────────────────────────────────

    @Test
    void createReservation_withAvailableSeat_returnsReservation() {
        when(userRepository.findByEmail("test@cinevault.com")).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(1L))).thenReturn(List.of(seat));
        when(seatRepository.findAvailableSeatsForShowtime(1L, 1L)).thenReturn(List.of(seat));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation("test@cinevault.com", request);

        assertNotNull(result);
        assertEquals(new BigDecimal("7.00"), result.getTotalPrice());
        assertEquals(Reservation.ReservationStatus.HELD, result.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
        verify(seatService).evictSeatCache();
    }

    @Test
    void createReservation_withUnavailableSeat_throwsException() {
        when(userRepository.findByEmail("test@cinevault.com")).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(1L))).thenReturn(List.of(seat));
        // Return empty list — seat is not available
        when(seatRepository.findAvailableSeatsForShowtime(1L, 1L)).thenReturn(List.of());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> reservationService.createReservation("test@cinevault.com", request)
        );

        assertTrue(exception.getMessage().contains("is no longer available"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_withVipSeat_appliesSurcharge() {
        seat.setType(Seat.SeatType.VIP);

        when(userRepository.findByEmail("test@cinevault.com")).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(1L))).thenReturn(List.of(seat));
        when(seatRepository.findAvailableSeatsForShowtime(1L, 1L)).thenReturn(List.of(seat));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation("test@cinevault.com", request);

        // VIP surcharge of £3 added to base price of £7 = £10
        assertEquals(new BigDecimal("10.00"), result.getTotalPrice());
    }

    // ── Cancel Reservation Tests ──────────────────────────────────────────

    @Test
    void cancelReservation_withHeldReservation_cancelsSuccessfully() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .status(Reservation.ReservationStatus.HELD)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.cancelReservation(1L, "test@cinevault.com");

        assertEquals(Reservation.ReservationStatus.CANCELLED, result.getStatus());
        verify(seatService).evictSeatCache();
        // No cancellation email for HELD reservations
        verify(bookingConfirmationService, never()).sendCancellationEmail(any());
    }

    @Test
    void cancelReservation_withConfirmedReservation_sendsCancellationEmail() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .status(Reservation.ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        reservationService.cancelReservation(1L, "test@cinevault.com");

        verify(bookingConfirmationService).sendCancellationEmail(any(Reservation.class));
    }

    @Test
    void cancelReservation_alreadyCancelled_throwsException() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .user(user)
                .status(Reservation.ReservationStatus.CANCELLED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> reservationService.cancelReservation(1L, "test@cinevault.com")
        );

        assertEquals("Reservation is already cancelled.", exception.getMessage());
    }
}