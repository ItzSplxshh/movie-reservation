package com.moviereservation.service;

import com.moviereservation.dto.AuthRequest;
import com.moviereservation.dto.AuthResponse;
import com.moviereservation.dto.RegisterRequest;
import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for user authentication and registration.
 * Handles new user account creation with password encoding and
 * existing user login with credential validation.
 * Returns a signed JWT token on success for use in subsequent requests.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;

    /**
     * Registers a new user account and returns a JWT token.
     * Validates that the email address is not already in use before
     * creating the account. The password is encoded using BCrypt before
     * being stored. The new user is assigned the USER role by default.
     * A JWT token is generated immediately so the user is authenticated
     * without needing to log in separately after registration.
     *
     * @param req the registration request containing email, password and name
     * @return an AuthResponse containing the JWT token and user details
     * @throws IllegalArgumentException if the email address is already in use
     */
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        // Generate JWT token so the user is immediately authenticated
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(),
                user.getFirstName() + " " + user.getLastName(), user.getRole().name());
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     * Delegates credential validation to Spring Security's AuthenticationManager
     * which verifies the email and BCrypt-hashed password against the database.
     * Throws a BadCredentialsException if the credentials are invalid,
     * resulting in a 401 Unauthorized response.
     *
     * @param req the login request containing email and password
     * @return an AuthResponse containing the JWT token and user details
     * @throws BadCredentialsException if the email or password is incorrect
     */
    public AuthResponse login(AuthRequest req) {
        // Delegate credential validation to Spring Security
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate and return a signed JWT token for the authenticated user
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(),
                user.getFirstName() + " " + user.getLastName(), user.getRole().name());
    }
}