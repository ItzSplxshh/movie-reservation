package com.moviereservation.config;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.superadmin.email:admin@cinevault.com}")
    private String superAdminEmail;

    @Value("${app.superadmin.password:Admin@123}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        // Create Super Admin account if it doesn't already exist
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