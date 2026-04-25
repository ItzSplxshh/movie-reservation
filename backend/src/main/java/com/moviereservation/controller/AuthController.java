package com.moviereservation.controller;

import com.moviereservation.dto.*;
import com.moviereservation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication.
 * Handles user registration and login requests.
 * Both endpoints are publicly accessible and do not require a JWT token.
 * Returns an AuthResponse containing a JWT token on success,
 * which the client stores and includes in subsequent authenticated requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * Validates the request body using Bean Validation before processing.
     * Returns a JWT token on successful registration so the user is
     * immediately authenticated without needing to log in separately.
     *
     * @param req the registration request containing email, password and name
     * @return an AuthResponse containing the JWT token and user details
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    /**
     * Authenticates an existing user.
     * Validates credentials against the database and returns a signed JWT token
     * on success. Throws an exception if the email does not exist or the
     * password does not match, resulting in a 401 Unauthorized response.
     *
     * @param req the login request containing email and password
     * @return an AuthResponse containing the JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}