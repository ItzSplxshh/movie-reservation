package com.moviereservation.controller;

import com.moviereservation.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a PaymentIntent for a reservation.
     * Returns clientSecret to complete payment on the frontend.
     */
    @PostMapping("/create-intent/{reservationId}")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @PathVariable Long reservationId) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(reservationId));
    }

    /**
     * Stripe webhook endpoint — receives payment confirmation events.
     * Must be excluded from CSRF and JWT filtering.
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
