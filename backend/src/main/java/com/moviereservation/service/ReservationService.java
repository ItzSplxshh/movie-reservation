package com.moviereservation;

import com.moviereservation.dto.ReservationRequest;
import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingConfirmationService bookingConfirmationService;
    private final SeatService seatService;
    private final SnackRepository snackRepository;

    public List<Seat> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        return seatRepository.findAvailableSeatsForShowtime(
                showtime.getTheater().getId(), showtimeId);
    }

    @Transactional
    public Reservation createReservation(String userEmail, ReservationRequest req) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Showtime showtime = showtimeRepository.findById(req.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        List<Seat> requestedSeats = seatRepository.findAllById(req.getSeatIds());

        if (requestedSeats.size() != req.getSeatIds().size()) {
            throw new RuntimeException("One or more seats not found");
        }

        // Verify seats are still available
        List<Seat> available = seatRepository.findAvailableSeatsForShowtime(
                showtime.getTheater().getId(), req.getShowtimeId());
        List<Long> availableIds = available.stream().map(Seat::getId).toList();

        for (Seat seat : requestedSeats) {
            if (!availableIds.contains(seat.getId())) {
                throw new RuntimeException("Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is no longer available");
            }
        }

        BigDecimal vipSurcharge = new BigDecimal("3.00");
        BigDecimal total = requestedSeats.stream()
                .map(seat -> seat.getType() == Seat.SeatType.VIP
                        ? showtime.getTicketPrice().add(vipSurcharge)
                        : showtime.getTicketPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate snack total
        BigDecimal snackTotal = BigDecimal.ZERO;
        Map<Long, Integer> snackMap = new HashMap<>();
        if (req.getSnacks() != null && !req.getSnacks().isEmpty()) {
            for (Map.Entry<Long, Integer> entry : req.getSnacks().entrySet()) {
                Snack snack = snackRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Snack not found: " + entry.getKey()));
                snackTotal = snackTotal.add(snack.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
                snackMap.put(entry.getKey(), entry.getValue());
            }
        }

        BigDecimal finalTotal = total.add(snackTotal);

        Reservation reservation = Reservation.builder()
                .user(user)
                .showtime(showtime)
                .seats(requestedSeats)
                .totalPrice(finalTotal)
                .snacks(snackMap)
                .build();

        // Start the 15 minute hold timer
        reservation.initializeHold();

        seatService.evictSeatCache();

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getUserReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUserId(user.getId());
    }

    public Reservation getReservationById(Long id, String userEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!reservation.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }
        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long id, String userEmail) {
        Reservation reservation = getReservationById(id, userEmail);
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled.");
        }
        boolean wasConfirmed = reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED;
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        //Send Cancellation email
        if (wasConfirmed) {
            bookingConfirmationService.sendCancellationEmail(saved);
        }
        seatService.evictSeatCache();

        return saved;
    }
}
