package com.moviereservation.repository;

import com.moviereservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Custom query methods support authentication, registration
 * validation and password reset token lookup.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Used by AuthService for login, JwtAuthFilter for token validation,
     * and UserController for profile and password management.
     *
     * @param email the email address to search for
     * @return an Optional containing the matching user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email address already exists.
     * Used by AuthService during registration to prevent duplicate accounts
     * before attempting to save a new user.
     *
     * @param email the email address to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their password reset token.
     * Used by PasswordResetService to locate the account associated
     * with a reset token received from the password reset email link.
     *
     * @param resetToken the password reset token to search for
     * @return an Optional containing the matching user, or empty if the token is invalid
     */
    Optional<User> findByResetToken(String resetToken);
}