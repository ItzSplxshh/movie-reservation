package com.moviereservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a registered user account in CineVault.
 * Stores authentication credentials, personal details and role assignment.
 * Passwords are stored as BCrypt hashes and are excluded from JSON
 * serialization to prevent accidental exposure in API responses.
 */
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user's email address — used as the unique login identifier */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The user's BCrypt hashed password.
     * JsonIgnore ensures the password hash is never included in API responses,
     * preventing accidental exposure of credentials.
     */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /**
     * The user's role controlling access to system features.
     * USER — standard booking access.
     * ADMIN — full admin panel access.
     * SUPER_ADMIN — admin access with protection from modification or deletion.
     * Defaults to USER on registration.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /**
     * Timestamp when the user account was created.
     * Set automatically on first persist and cannot be updated.
     */
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Sets the createdAt timestamp before the entity is first persisted */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Time-limited token for password reset verification.
     * Generated when a password reset is requested and invalidated after use.
     */
    private String resetToken;

    /** Expiry timestamp for the password reset token — typically 1 hour from generation */
    private LocalDateTime resetTokenExpiry;

    /**
     * All reservations made by this user.
     * JsonIgnore prevents circular serialization.
     * Cascade ALL ensures reservations are deleted when a user is deleted.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    /** Possible roles for a user account */
    public enum Role {
        USER,        // Standard user with booking access
        ADMIN,       // Administrator with full admin panel access
        SUPER_ADMIN  // Protected administrator account that cannot be modified or deleted
    }
}