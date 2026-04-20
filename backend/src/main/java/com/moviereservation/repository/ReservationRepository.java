package com.moviereservation.repository;

import com.moviereservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    Optional<Reservation> findByStripePaymentIntentId(String paymentIntentId);
    List<Reservation> findByStatusAndHeldUntilBefore(
            Reservation.ReservationStatus status,
            LocalDateTime dateTime
    );
}
