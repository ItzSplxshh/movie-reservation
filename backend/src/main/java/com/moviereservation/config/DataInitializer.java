package com.moviereservation.config;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialises essential application data on startup.
 * Implements CommandLineRunner to execute after the Spring context
 * is fully loaded. Creates the Super Admin account if it does not
 * already exist, ensuring every deployment has an admin account
 * available without requiring manual database setup.
 * This is particularly important for fresh Docker deployments where
 * the database starts empty.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Super Admin email address — configurable via application.properties */
    @Value("${app.superadmin.email:admin@cinevault.com}")
    private String superAdminEmail;

    /** Super Admin password — configurable via application.properties */
    @Value("${app.superadmin.password:Admin@123}")
    private String superAdminPassword;

    /**
     * Runs on application startup after the Spring context is loaded.
     * Checks whether a Super Admin account already exists before creating one
     * to ensure idempotency — running the application multiple times will not
     * create duplicate Super Admin accounts.
     */
    @Override
    public void run(String... args) {
        // Only create the Super Admin if one does not already exist
        if (userRepository.findByEmail(superAdminEmail).isEmpty()) {
            User superAdmin = User.builder()
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .firstName("Super")
                    .lastName("Admin")
                    .role(User.Role.SUPER_ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("=== Super Admin account created: " + superAdminEmail + " ===");
        } else {
            System.out.println("=== Super Admin account already exists ===");
        }
    }
}