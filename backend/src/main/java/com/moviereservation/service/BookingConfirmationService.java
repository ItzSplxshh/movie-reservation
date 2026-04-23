package com.moviereservation.service;

import com.moviereservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BookingConfirmationService {

    private final JavaMailSender mailSender;

    public void sendConfirmationEmail(Reservation reservation) {
        try {
            String userEmail = reservation.getUser().getEmail();
            String firstName = reservation.getUser().getFirstName();
            String movieTitle = reservation.getShowtime().getMovie().getTitle();
            String theaterName = reservation.getShowtime().getTheater().getName();
            String startTime = reservation.getShowtime().getStartTime()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' HH:mm"));
            String seats = reservation.getSeats().stream()
                    .map(s -> s.getRowLabel() + s.getSeatNumber())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A");
            String total = "$" + reservation.getTotalPrice().toPlainString();

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
                            "🎫 Booking Reference: " + reservation.getBookingReference() + "\n\n" +
                            "Please keep this reference safe — you may need it at the cinema.\n\n" +
                            "Enjoy the film!\n\n" +
                            "The CineVault Team"
            );

            mailSender.send(message);
            System.out.println("Confirmation email sent to: " + userEmail);
        } catch (Exception e) {
            System.out.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    public void sendCancellationEmail(Reservation reservation) {
        try {
            String userEmail = reservation.getUser().getEmail();
            String firstName = reservation.getUser().getFirstName();
            String movieTitle = reservation.getShowtime().getMovie().getTitle();
            String theaterName = reservation.getShowtime().getTheater().getName();
            String startTime = reservation.getShowtime().getStartTime()
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' HH:mm"));
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
                            "💺 Seats: " + seats + "\n\n" +
                            "If you cancelled by mistake, please make a new booking.\n\n" +
                            "The CineVault Team"
            );

            mailSender.send(message);
            System.out.println("Cancellation email sent to: " + userEmail);
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
        }
    }
}
