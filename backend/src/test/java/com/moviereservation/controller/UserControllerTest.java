package com.moviereservation.controller;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.service.BookingConfirmationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BookingConfirmationService bookingConfirmationService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@cinevault.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();

        when(userDetails.getUsername()).thenReturn("test@cinevault.com");
        when(userRepository.findByEmail("test@cinevault.com")).thenReturn(Optional.of(user));
    }

    // ── Get Profile Tests ─────────────────────────────────────────────────

    @Test
    void getProfile_withValidUser_returnsUser() {
        ResponseEntity<?> response = userController.getProfile(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
    }

    // ── Update Profile Tests ──────────────────────────────────────────────

    @Test
    void updateProfile_withValidNames_updatesSuccessfully() {
        Map<String, String> body = Map.of("firstName", "Jane", "lastName", "Smith");

        ResponseEntity<?> response = userController.updateProfile(userDetails, body);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_withBlankFirstName_doesNotUpdateFirstName() {
        user.setFirstName("John");
        Map<String, String> body = Map.of("firstName", "  ", "lastName", "Smith");

        userController.updateProfile(userDetails, body);

        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
    }

    // ── Change Password Tests ─────────────────────────────────────────────

    @Test
    void changePassword_withCorrectCurrentPassword_updatesSuccessfully() {
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "newPassword123",
                "confirmPassword", "newPassword123"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository).save(user);
        verify(bookingConfirmationService).sendPasswordChangedEmail(user);
    }

    @Test
    void changePassword_withWrongCurrentPassword_returnsBadRequest() {
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        Map<String, String> body = Map.of(
                "currentPassword", "wrongPass",
                "newPassword", "newPassword123",
                "confirmPassword", "newPassword123"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_withMismatchedPasswords_returnsBadRequest() {
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "newPassword123",
                "confirmPassword", "differentPassword"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_withTooShortPassword_returnsBadRequest() {
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "abc",
                "confirmPassword", "abc"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_sendsConfirmationEmail() {
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "newPassword123",
                "confirmPassword", "newPassword123"
        );

        userController.changePassword(userDetails, body);

        verify(bookingConfirmationService).sendPasswordChangedEmail(user);
    }

    // ── Boundary Value Analysis Tests ─────────────────────────────────────
    // Testing password length boundaries for password change

    @Test
    void changePassword_withPasswordExactly6Characters_succeeds() {
        // Boundary value — exactly at the minimum valid password length
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "abc123",
                "confirmPassword", "abc123"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withPasswordOf5Characters_returnsBadRequest() {
        // Boundary value — one below the minimum valid password length
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "ab123",
                "confirmPassword", "ab123"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    // ── Equivalence Partitioning Tests ────────────────────────────────────
    // Testing valid and invalid input partitions for password change

    @Test
    void changePassword_withLongValidPassword_succeeds() {
        // Equivalence partition — valid password well above minimum length
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "ThisIsAVeryLongAndSecurePassword123",
                "confirmPassword", "ThisIsAVeryLongAndSecurePassword123"
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withEmptyNewPassword_returnsBadRequest() {
        // Equivalence partition — invalid partition, empty password
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);

        Map<String, String> body = Map.of(
                "currentPassword", "currentPass",
                "newPassword", "",
                "confirmPassword", ""
        );

        ResponseEntity<?> response = userController.changePassword(userDetails, body);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }
}