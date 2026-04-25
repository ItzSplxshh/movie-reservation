package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for automatically expiring held reservations.
 * Runs as a scheduled background job every 60 seconds to identify
 * reservations in HELD status whose hold timer has expired.
 * Expired reservations are cancelled and their seats released back
 * to the available pool, preventing indefinitely held seats.
 * This service is a core component of the double booking prevention mechanism.
 */
@Service
@RequiredArgsConstructor
public class ReservationExpiryService {

    private final ReservationRepository reservationRepository;

    /**
     * Finds and cancels all held reservations whose expiry time has passed.
     * Runs automatically every 60 seconds via Spring's @Scheduled mechanism.
     * Wrapped in a transaction to ensure all cancellations in a single run
     * are committed atomically.
     * A reservation is considered expired when its heldUntil timestamp
     * is before the current time, meaning the 15-minute payment window has passed.
     * Once cancelled, the seats associated with the reservation become available
     * for other users to book on their next seat availability request.
     */
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    @Transactional
    public void expireHeldReservations() {
        // Find all HELD reservations whose hold timer has expired
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndHeldUntilBefore(
                        Reservation.ReservationStatus.HELD,
                        LocalDateTime.now(java.time.ZoneOffset.UTC)
                );

        // Cancel each expired reservation
        for (Reservation reservation : expiredReservations) {
            System.out.println("Expiring reservation ID: " + reservation.getId());
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }

        if (!expiredReservations.isEmpty()) {
            System.out.println("Expired " + expiredReservations.size() + " held reservations");
        }
    }
}
