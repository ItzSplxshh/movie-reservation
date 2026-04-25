package com.moviereservation.repository;

import com.moviereservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Reservation database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Custom query methods support the seat hold expiry mechanism
 * and Stripe webhook payment confirmation flow.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Finds all reservations belonging to a specific user.
     * Used by the My Tickets page to display a user's booking history
     * including held, confirmed and cancelled reservations.
     *
     * @param userId the ID of the user whose reservations to retrieve
     * @return a list of all reservations for the given user
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * Finds a reservation by its Stripe PaymentIntent ID.
     * Used by the webhook handler to locate the correct reservation
     * when a payment_intent.succeeded or payment_intent.payment_failed
     * event is received from Stripe.
     *
     * @param paymentIntentId the Stripe PaymentIntent ID to search for
     * @return an Optional containing the matching reservation, or empty if not found
     */
    Optional<Reservation> findByStripePaymentIntentId(String paymentIntentId);

    /**
     * Finds all reservations with a specific status whose hold has expired.
     * Used by the scheduled background job to identify HELD reservations
     * that have passed their heldUntil timestamp and should be cancelled,
     * releasing the seats back to the available pool.
     *
     * @param status   the reservation status to filter by — typically HELD
     * @param dateTime the cutoff timestamp — reservations held until before this time are returned
     * @return a list of expired reservations matching the given status
     */
    List<Reservation> findByStatusAndHeldUntilBefore(
            Reservation.ReservationStatus status,
            LocalDateTime dateTime
    );
}