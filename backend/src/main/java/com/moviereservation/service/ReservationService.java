package com.moviereservation.service;

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

/**
 * Service responsible for managing cinema seat reservations.
 * Handles the core booking flow including seat availability verification,
 * double booking prevention, price calculation with VIP surcharges and
 * snack pre-orders, seat hold initialisation and reservation cancellation.
 * Cache eviction is triggered after any operation that changes seat availability.
 */
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

    /**
     * Returns all currently available seats for a given showtime.
     * Excludes seats already held, pending or confirmed by other reservations.
     * Used by the frontend to determine which seats to show as available on the seat map.
     *
     * @param showtimeId the ID of the showtime to check availability for
     * @return a list of available seats for the showtime
     */
    public List<Seat> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
        return seatRepository.findAvailableSeatsForShowtime(
                showtime.getTheater().getId(), showtimeId);
    }

    /**
     * Creates a new reservation with a 15-minute seat hold.
     * Validates that all requested seats exist and are still available
     * at the moment of booking to prevent double booking.
     * Calculates the total price including base ticket price, VIP surcharges
     * of $3.00 per VIP seat, and the cost of any pre-ordered snacks.
     * Evicts the seat availability cache after creation so subsequent
     * requests reflect the newly held seats.
     *
     * @param userEmail the email of the authenticated user making the reservation
     * @param req       the reservation request containing showtime ID, seat IDs and snacks
     * @return the created reservation with HELD status and 15-minute expiry
     * @throws RuntimeException if any requested seat is no longer available
     */
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

        // Verify all requested seats are still available at the moment of booking
        List<Seat> available = seatRepository.findAvailableSeatsForShowtime(
                showtime.getTheater().getId(), req.getShowtimeId());
        List<Long> availableIds = available.stream().map(Seat::getId).toList();

        for (Seat seat : requestedSeats) {
            if (!availableIds.contains(seat.getId())) {
                throw new RuntimeException("Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is no longer available");
            }
        }

        // Calculate seat total applying $3.00 VIP surcharge where applicable
        BigDecimal vipSurcharge = new BigDecimal("3.00");
        BigDecimal total = requestedSeats.stream()
                .map(seat -> seat.getType() == Seat.SeatType.VIP
                        ? showtime.getTicketPrice().add(vipSurcharge)
                        : showtime.getTicketPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate snack total and build snack map for the reservation
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

        // Start the 15-minute seat hold timer
        reservation.initializeHold();

        // Evict cache so subsequent seat map requests reflect the newly held seats
        seatService.evictSeatCache();

        return reservationRepository.save(reservation);
    }

    /**
     * Returns all reservations belonging to the authenticated user.
     * Includes reservations in all statuses — HELD, CONFIRMED, CANCELLED.
     * Used by the My Tickets page to display the user's booking history.
     *
     * @param userEmail the email of the authenticated user
     * @return a list of all reservations for the given user
     */
    public List<Reservation> getUserReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reservationRepository.findByUserId(user.getId());
    }

    /**
     * Returns a specific reservation by ID, restricted to the reservation owner.
     * Throws an exception if the authenticated user does not own the reservation,
     * preventing users from accessing other users' booking details.
     *
     * @param id        the ID of the reservation to retrieve
     * @param userEmail the email of the authenticated user
     * @return the reservation with the given ID
     * @throws RuntimeException if the reservation does not belong to the authenticated user
     */
    public Reservation getReservationById(Long id, String userEmail) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!reservation.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied");
        }
        return reservation;
    }

    /**
     * Cancels a reservation and releases the held seats.
     * Sends a cancellation email only if the reservation was previously CONFIRMED,
     * not for expired HELD reservations which are cancelled automatically.
     * Evicts the seat availability cache after cancellation so the freed seats
     * are immediately available for other users to book.
     *
     * @param id        the ID of the reservation to cancel
     * @param userEmail the email of the authenticated user
     * @return the cancelled reservation
     * @throws RuntimeException if the reservation is already cancelled
     */
    @Transactional
    public Reservation cancelReservation(Long id, String userEmail) {
        Reservation reservation = getReservationById(id, userEmail);
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled.");
        }

        // Only send cancellation email if the reservation was previously confirmed
        boolean wasConfirmed = reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED;
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        if (wasConfirmed) {
            bookingConfirmationService.sendCancellationEmail(saved);
        }

        // Evict cache so the freed seats are immediately available to other users
        seatService.evictSeatCache();

        return saved;
    }
}