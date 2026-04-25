package com.moviereservation.controller;

import com.moviereservation.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST controller for Stripe payment processing.
 * Handles PaymentIntent creation for the checkout flow and
 * receives asynchronous payment events from Stripe via webhooks.
 * The webhook endpoint is publicly accessible and excluded from
 * JWT authentication as Stripe sends events directly from its servers.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a Stripe PaymentIntent for a given reservation.
     * Returns the clientSecret to the React frontend for use with Stripe Elements.
     * Requires the reservation to be in a HELD or PENDING state.
     * If a PaymentIntent already exists for this reservation, the existing
     * one is returned to prevent duplicate charges on page refresh.
     *
     * @param reservationId the ID of the reservation to create a payment for
     * @return a map containing the clientSecret and paymentIntentId
     */
    @PostMapping("/create-intent/{reservationId}")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @PathVariable Long reservationId) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(reservationId));
    }

    /**
     * Stripe webhook endpoint that receives asynchronous payment events.
     * Verifies the Stripe-Signature header to confirm the event originated
     * from Stripe, then delegates to PaymentService for processing.
     * Handles payment_intent.succeeded to confirm reservations and
     * payment_intent.payment_failed to cancel them.
     * Returns 400 Bad Request if the webhook signature is invalid.
     *
     * @param payload   the raw JSON payload from Stripe
     * @param sigHeader the Stripe-Signature header for signature verification
     * @return a success message or error details if processing fails
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            System.out.println("=== WEBHOOK ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}