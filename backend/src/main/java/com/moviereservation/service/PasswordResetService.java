package com.moviereservation.service;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for the password reset flow.
 * Handles generating and sending password reset tokens via email,
 * and applying new passwords after token validation.
 * Tokens are time-limited to 1 hour to reduce the window of
 * vulnerability if a reset email is intercepted.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    /**
     * Generates a password reset token and sends a reset email.
     * Creates a unique UUID token, stores it against the user account
     * with a 1-hour expiry timestamp, then sends an email containing
     * a reset link to the user's registered email address.
     * The reset link directs the user to the frontend reset password page
     * with the token as a URL parameter.
     *
     * @param email the email address of the account to reset
     * @throws RuntimeException if no account exists with the given email
     */
    public void sendResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        // Generate a unique token and set a 1-hour expiry
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Click the link below to reset your password:\n\n" +
                "http://localhost:3000/reset-password?token=" + token + "\n\n" +
                "This link expires in 1 hour.\n\n" +
                "If you didn't request this, ignore this email.");

        mailSender.send(message);
    }

    /**
     * Resets a user's password using a valid reset token.
     * Validates that the token exists and has not expired before
     * applying the new BCrypt-encoded password.
     * Clears the reset token and expiry after successful use
     * to prevent the same token from being used more than once.
     *
     * @param token       the reset token received from the email link
     * @param newPassword the new password to set for the account
     * @throws RuntimeException if the token is invalid or has expired
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Verify the token has not passed its 1-hour expiry
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Encode and apply the new password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Invalidate the token to prevent reuse
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}