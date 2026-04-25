package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import com.moviereservation.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.moviereservation.entity.User;

/**
 * Service responsible for sending transactional email notifications.
 * Uses Spring Mail with Gmail SMTP to send booking confirmations,
 * cancellations and account security notifications.
 * All methods wrap email sending in try-catch to ensure that email
 * failures do not interrupt the core booking or authentication flow.
 */
@Service
@RequiredArgsConstructor
public class BookingConfirmationService {

    private final JavaMailSender mailSender;
    private final SnackRepository snackRepository;

    /**
     * Sends a booking confirmation email to the user after successful payment.
     * Includes movie title, showtime, theatre, seat numbers, snacks pre-ordered,
     * total price and unique booking reference code.
     * Called by PaymentService after the Stripe webhook confirms payment.
     *
     * @param reservation the confirmed reservation containing all booking details
     */
    public void sendConfirmationEmail(Reservation reservation) {
        try {
            // Extract booking details from the reservation
            String userEmail = reservation.getUser().getEmail();
            String firstName = reservation.getUser().getFirstName();
            String movieTitle = reservation.getShowtime().getMovie().getTitle();
            String theaterName = reservation.getShowtime().getTheater().getName();
            String startTime = reservation.getShowtime().getStartTime()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' HH:mm"));

            // Format seat labels e.g. A1, A2, B3
            String seats = reservation.getSeats().stream()
                    .map(s -> s.getRowLabel() + s.getSeatNumber())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A");
            String total = "$" + reservation.getTotalPrice().toPlainString();

            // Build snacks summary if any snacks were pre-ordered
            String snacksSummary = "";
            if (reservation.getSnacks() != null && !reservation.getSnacks().isEmpty()) {
                StringBuilder sb = new StringBuilder("\n🍿 Snacks Pre-ordered:\n");
                for (Map.Entry<Long, Integer> entry : reservation.getSnacks().entrySet()) {
                    // Look up each snack by ID to get its name and price
                    snackRepository.findById(entry.getKey()).ifPresent(snack -> {
                        sb.append("   • ").append(snack.getEmoji()).append(" ")
                                .append(snack.getName()).append(" x").append(entry.getValue())
                                .append(" — $").append(String.format("%.2f", snack.getPrice().multiply(
                                        java.math.BigDecimal.valueOf(entry.getValue()))))
                                .append("\n");
                    });
                }
                snacksSummary = sb.toString();
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Booking Confirmed - " + movieTitle);
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "Your booking is confirmed! Here are your details:\n\n" +
                            "🎬 Movie: " + movieTitle + "\n" +
                            "📅 Date: " + startTime + "\n" +
                            "🎭 Theatre: " + theaterName + "\n" +
                            "💺 Seats: " + seats + "\n" +
                            "💰 Total: " + total + "\n\n" +
                            snacksSummary + "\n" +
                            "🎫 Booking Reference: " + reservation.getBookingReference() + "\n\n" +
                            "Please keep this reference safe — you may need it at the cinema.\n\n" +
                            "Enjoy the film!\n\n" +
                            "The CineVault Team"
            );

            mailSender.send(message);
            System.out.println("Confirmation email sent to: " + userEmail);
        } catch (Exception e) {
            // Log failure but do not throw — email failure should not affect booking status
            System.out.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    /**
     * Sends a cancellation email to the user when a confirmed reservation is cancelled.
     * Only sent for previously confirmed reservations — not for expired holds.
     * Called by ReservationService after updating the reservation status to CANCELLED.
     *
     * @param reservation the cancelled reservation containing booking details
     */
    public void sendCancellationEmail(Reservation reservation) {
        try {
            String userEmail = reservation.getUser().getEmail();
            String firstName = reservation.getUser().getFirstName();
            String movieTitle = reservation.getShowtime().getMovie().getTitle();
            String theaterName = reservation.getShowtime().getTheater().getName();
            String startTime = reservation.getShowtime().getStartTime()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' HH:mm"));

            // Format seat labels for the cancellation summary
            String seats = reservation.getSeats().stream()
                    .map(s -> s.getRowLabel() + s.getSeatNumber())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Booking Cancelled - " + movieTitle);
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "Your booking has been cancelled. Here were your details:\n\n" +
                            "🎬 Movie: " + movieTitle + "\n" +
                            "📅 Date: " + startTime + "\n" +
                            "🎭 Theatre: " + theaterName + "\n" +
                            "💺 Seats: " + seats + "\n" +
                            "🎫 Booking Reference: " + reservation.getBookingReference() + "\n\n" +
                            "If you cancelled by mistake, please make a new booking.\n\n" +
                            "The CineVault Team"
            );

            mailSender.send(message);
            System.out.println("Cancellation email sent to: " + userEmail);
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    /**
     * Sends a security notification email when a user changes their password.
     * Advises the user to reset their password immediately if they did not
     * initiate the change, providing an early warning against unauthorised access.
     *
     * @param user the user whose password was changed
     */
    public void sendPasswordChangedEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Your CineVault Password Has Been Changed");
            message.setText(
                    "Hi " + user.getFirstName() + ",\n\n" +
                            "Your CineVault account password was successfully changed.\n\n" +
                            "If you did not make this change, please reset your password " +
                            "immediately using the 'Forgot Password' link on the login page.\n\n" +
                            "The CineVault Team"
            );
            mailSender.send(message);
            System.out.println("Password changed email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send password changed email: " + e.getMessage());
        }
    }
}