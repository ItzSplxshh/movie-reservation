package com.moviereservation.service;

import com.google.gson.JsonObject;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final ReservationRepository reservationRepository;

    private final BookingConfirmationService bookingConfirmationService;

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

        if (reservation.getStatus() != Reservation.ReservationStatus.HELD
                && reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not in a payable state");
        }

        if (reservation.getStripePaymentIntentId() != null) {
            return Map.of(
                    "clientSecret", reservation.getStripeClientSecret(),
                    "paymentIntentId", reservation.getStripePaymentIntentId()
            );
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

        System.out.println("=== WEBHOOK RECEIVED ===");
        System.out.println("Event type: " + event.getType());

        // Only process payment_intent.succeeded events
        if ("payment_intent.succeeded".equals(event.getType())) {
            // Parse the raw JSON payload to extract the payment intent ID
            JsonObject eventJson = new com.google.gson.JsonParser().parse(payload).getAsJsonObject();
            JsonObject data = eventJson.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();

            System.out.println("Processing payment success for: " + paymentIntentId);
            confirmReservation(paymentIntentId);
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            JsonObject eventJson = new com.google.gson.JsonParser().parse(payload).getAsJsonObject();
            JsonObject data = eventJson.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();

            System.out.println("Processing payment failure for: " + paymentIntentId);
            cancelReservationPayment(paymentIntentId);
        }
    }

    private void confirmReservation(String paymentIntentId) {
        System.out.println("=== CONFIRMING RESERVATION ===");
        System.out.println("Payment Intent ID: " + paymentIntentId);

        Optional<Reservation> reservationOpt = reservationRepository.findByStripePaymentIntentId(paymentIntentId);

        if (reservationOpt.isEmpty()) {
            System.out.println("ERROR: Reservation not found for payment intent: " + paymentIntentId);
            throw new RuntimeException("Reservation not found for payment intent: " + paymentIntentId);
        }

        Reservation reservation = reservationOpt.get();
        System.out.println("Found reservation ID: " + reservation.getId());
        System.out.println("Current status: " + reservation.getStatus());

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setPaidAt(LocalDateTime.now());
        reservation.setHeldUntil(null); // clear the hold timer
        // Generate unique booking reference
        if (reservation.getBookingReference() == null) {
            String ref = "CV-" + LocalDateTime.now().getYear() + "-" +
                    java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            reservation.setBookingReference(ref);
        }

        reservationRepository.save(reservation);

        // Send confirmation email
        bookingConfirmationService.sendConfirmationEmail(reservation);

        System.out.println("Reservation updated to CONFIRMED");
    }

    private void cancelReservationPayment(String paymentIntentId) {
        reservationRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(reservation -> {
                    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                });
    }
}
