package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationExpiryService {

    private final ReservationRepository reservationRepository;

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHeldReservations() {
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndHeldUntilBefore(
                        Reservation.ReservationStatus.HELD,
                        LocalDateTime.now()
                );

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
