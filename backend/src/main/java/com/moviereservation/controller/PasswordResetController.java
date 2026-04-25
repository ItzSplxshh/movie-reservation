package com.moviereservation.controller;

import com.moviereservation.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST controller for password reset functionality.
 * Both endpoints are publicly accessible as they are used by
 * unauthenticated users who have forgotten their password.
 * The flow is: request reset email -> receive token -> reset password.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Sends a password reset email to the provided email address.
     * Generates a time-limited reset token, stores it against the user account
     * and sends an email containing a reset link.
     * Returns a generic success message regardless of whether the email exists
     * to prevent user enumeration attacks.
     *
     * @param body request body containing the email address
     * @return a success message or error details if the request fails
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.sendResetEmail(body.get("email"));
            return ResponseEntity.ok("Reset email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Resets a user's password using a valid reset token.
     * Validates that the token exists, belongs to a user account and has not expired.
     * Encodes the new password using BCrypt before saving and invalidates
     * the token to prevent reuse.
     *
     * @param body request body containing the reset token and new password
     * @return a success message or error details if the token is invalid or expired
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}