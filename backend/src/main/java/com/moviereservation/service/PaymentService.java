package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.ReservationRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final ReservationRepository reservationRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Create a Stripe Payment Intent for a reservation.
     * Returns the clientSecret to use on the frontend with Stripe.js.
     */
    public Map<String, String> createPaymentIntent(Long reservationId) throws StripeException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not in PENDING state");
        }

        long amountInCents = reservation.getTotalPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setDescription("Movie tickets - Reservation #" + reservation.getId())
                .putMetadata("reservation_id", reservationId.toString())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save the PaymentIntent ID to the reservation
        reservation.setStripePaymentIntentId(paymentIntent.getId());
        reservation.setStripeClientSecret(paymentIntent.getClientSecret());
        reservationRepository.save(reservation);

        return Map.of(
                "clientSecret", paymentIntent.getClientSecret(),
                "paymentIntentId", paymentIntent.getId()
        );
    }

    /**
     * Handle Stripe webhook events.
     */
    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws StripeException {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe webhook signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElseThrow();
                confirmReservation(pi.getId());
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElseThrow();
                cancelReservationPayment(pi.getId());
            }
        }
    }

    private void confirmReservation(String paymentIntentId) {
        reservationRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(reservation -> {
                    reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
                    reservation.setPaidAt(LocalDateTime.now());
                    reservationRepository.save(reservation);
                });
    }

    private void cancelReservationPayment(String paymentIntentId) {
        reservationRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(reservation -> {
                    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                });
    }
}
