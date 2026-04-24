package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private BookingConfirmationService bookingConfirmationService;

    @InjectMocks
    private PaymentService paymentService;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
                .id(1L)
                .totalPrice(new BigDecimal("14.00"))
                .status(Reservation.ReservationStatus.HELD)
                .build();
    }

    // ── Create Payment Intent Tests ───────────────────────────────────────

    @Test
    void createPaymentIntent_withReservationNotFound_throwsException() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPaymentIntent(99L)
        );
    }

    @Test
    void createPaymentIntent_withAlreadyConfirmedReservation_throwsException() {
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.createPaymentIntent(1L)
        );

        assertEquals("Reservation is not in a payable state", exception.getMessage());
    }

    @Test
    void createPaymentIntent_withCancelledReservation_throwsException() {
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.createPaymentIntent(1L)
        );

        assertEquals("Reservation is not in a payable state", exception.getMessage());
    }

    @Test
    void createPaymentIntent_withExistingPaymentIntent_returnsExisting() throws Exception {
        reservation.setStripePaymentIntentId("pi_existing123");
        reservation.setStripeClientSecret("secret_existing");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        var result = paymentService.createPaymentIntent(1L);

        assertEquals("pi_existing123", result.get("paymentIntentId"));
        assertEquals("secret_existing", result.get("clientSecret"));
    }

    // ── Webhook Tests ─────────────────────────────────────────────────────

    @Test
    void handleWebhook_withInvalidSignature_throwsException() {
        String payload = "{\"type\": \"payment_intent.succeeded\"}";
        String invalidSig = "invalid_signature";

        assertThrows(
                Exception.class,
                () -> paymentService.handleWebhook(payload, invalidSig)
        );
    }
}
