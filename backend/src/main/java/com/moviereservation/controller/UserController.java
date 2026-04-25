package com.moviereservation.controller;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.service.BookingConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST controller for user account management.
 * All endpoints require authentication — users can only access
 * and modify their own account data extracted from the JWT token.
 * Provides profile viewing and updating, and secure password changing
 * with current password verification.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingConfirmationService bookingConfirmationService;

    /**
     * Returns the profile of the currently authenticated user.
     * The user is identified from the JWT token via @AuthenticationPrincipal,
     * ensuring users can only access their own profile data.
     *
     * @param userDetails the authenticated user extracted from the JWT token
     * @return the user entity for the authenticated user
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    /**
     * Updates the first and last name of the authenticated user.
     * Only updates fields that are present and non-blank in the request body,
     * allowing partial updates without overwriting existing values.
     * Email address cannot be changed via this endpoint.
     *
     * @param userDetails the authenticated user extracted from the JWT token
     * @param body        request body containing optional firstName and lastName fields
     * @return a success message on successful update
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update fields that are present and not blank
        if (body.containsKey("firstName") && !body.get("firstName").isBlank()) {
            user.setFirstName(body.get("firstName"));
        }
        if (body.containsKey("lastName") && !body.get("lastName").isBlank()) {
            user.setLastName(body.get("lastName"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    /**
     * Changes the password of the authenticated user.
     * Validates the current password before applying the change to prevent
     * unauthorised password changes if a session is compromised.
     * Enforces a minimum password length of 6 characters.
     * Sends a confirmation email after a successful change to alert
     * the user if the change was not initiated by them.
     *
     * @param userDetails the authenticated user extracted from the JWT token
     * @param body        request body containing currentPassword, newPassword and confirmPassword
     * @return a success message or a 400 Bad Request with an error message
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        // Verify the current password matches the stored BCrypt hash
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }

        // Ensure the new password and confirmation match
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "New passwords do not match"));
        }

        // Enforce minimum password length
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Notify the user by email that their password has been changed
        bookingConfirmationService.sendPasswordChangedEmail(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}