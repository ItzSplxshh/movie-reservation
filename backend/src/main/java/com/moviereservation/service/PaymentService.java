package com.moviereservation.service;

import com.google.gson.JsonObject;
import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.ReservationRepository;
import com.moviereservation.service.BookingConfirmationService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for managing Stripe payment processing.
 * Handles PaymentIntent creation and webhook event processing
 * for asynchronous payment confirmation and failure handling.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    /** Stripe secret API key injected from application properties */
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    /** Webhook signing secret used to verify events are from Stripe */
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final ReservationRepository reservationRepository;
    private final BookingConfirmationService bookingConfirmationService;

    /** Initialises the Stripe SDK with the API key on application startup */
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Creates a Stripe PaymentIntent for a given reservation.
     * Returns the clientSecret to the frontend for use with Stripe Elements.
     * If a PaymentIntent already exists for this reservation, the existing
     * one is returned to prevent duplicate charges.
     *
     * @param reservationId the ID of the reservation to create a payment for
     * @return a map containing the clientSecret and paymentIntentId
     */
    public Map<String, String> createPaymentIntent(Long reservationId) throws StripeException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Only HELD or PENDING reservations can be paid for
        if (reservation.getStatus() != Reservation.ReservationStatus.HELD
                && reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not in a payable state");
        }

        // Return existing PaymentIntent if one was already created for this reservation
        if (reservation.getStripePaymentIntentId() != null) {
            return Map.of(
                    "clientSecret", reservation.getStripeClientSecret(),
                    "paymentIntentId", reservation.getStripePaymentIntentId()
            );
        }

        // Convert total price to cents as required by the Stripe API
        long amountInCents = reservation.getTotalPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValue();

        // Build the PaymentIntent with amount, currency and reservation metadata
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

        // Persist the PaymentIntent ID and client secret to the reservation
        reservation.setStripePaymentIntentId(paymentIntent.getId());
        reservation.setStripeClientSecret(paymentIntent.getClientSecret());
        reservationRepository.save(reservation);

        return Map.of(
                "clientSecret", paymentIntent.getClientSecret(),
                "paymentIntentId", paymentIntent.getId()
        );
    }

    /**
     * Handles incoming Stripe webhook events.
     * Verifies the webhook signature to confirm the event originated from Stripe,
     * then routes to the appropriate handler based on the event type.
     * Processes payment_intent.succeeded and payment_intent.payment_failed events.
     *
     * @param payload   the raw JSON payload from Stripe
     * @param sigHeader the Stripe-Signature header for signature verification
     */
    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws StripeException {
        Event event;
        try {
            // Verify webhook signature to ensure the event is genuinely from Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe webhook signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            // Extract the PaymentIntent ID from the raw JSON payload
            JsonObject eventJson = new com.google.gson.JsonParser().parse(payload).getAsJsonObject();
            JsonObject data = eventJson.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();
            confirmReservation(paymentIntentId);

        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            JsonObject eventJson = new com.google.gson.JsonParser().parse(payload).getAsJsonObject();
            JsonObject data = eventJson.getAsJsonObject("data");
            JsonObject object = data.getAsJsonObject("object");
            String paymentIntentId = object.get("id").getAsString();
            cancelReservationPayment(paymentIntentId);
        }
    }

    /**
     * Confirms a reservation following successful payment.
     * Updates the reservation status to CONFIRMED, sets the payment timestamp,
     * generates a unique booking reference code and sends a confirmation email.
     * Called exclusively by the Stripe webhook handler.
     *
     * @param paymentIntentId the Stripe PaymentIntent ID from the webhook event
     */
    private void confirmReservation(String paymentIntentId) {
        Reservation reservation = reservationRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException(
                        "Reservation not found for payment intent: " + paymentIntentId));

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setPaidAt(LocalDateTime.now());

        // Clear the hold timer now that payment has been confirmed
        reservation.setHeldUntil(null);

        // Generate a unique booking reference in the format CV-YEAR-XXXXX
        if (reservation.getBookingReference() == null) {
            String ref = "CV-" + LocalDateTime.now().getYear() + "-" +
                    java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            reservation.setBookingReference(ref);
        }

        reservationRepository.save(reservation);

        // Send confirmation email with booking details and reference code
        bookingConfirmationService.sendConfirmationEmail(reservation);
    }

    /**
     * Cancels a reservation following a failed payment.
     * Updates the reservation status to CANCELLED.
     * Called exclusively by the Stripe webhook handler.
     *
     * @param paymentIntentId the Stripe PaymentIntent ID from the webhook event
     */
    private void cancelReservationPayment(String paymentIntentId) {
        reservationRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresent(reservation -> {
                    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                });
    }
}
