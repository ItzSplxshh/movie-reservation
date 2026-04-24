package com.moviereservation.service;

import com.moviereservation.dto.AuthRequest;
import com.moviereservation.dto.AuthResponse;
import com.moviereservation.dto.RegisterRequest;
import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@cinevault.com");
        registerRequest.setPassword("Password@123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        authRequest = new AuthRequest();
        authRequest.setEmail("test@cinevault.com");
        authRequest.setPassword("Password@123");

        user = User.builder()
                .email("test@cinevault.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.USER)
                .build();
    }

    // ── Register Tests ────────────────────────────────────────────────────

    @Test
    void register_withValidDetails_returnsAuthResponse() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mockJwtToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@cinevault.com", response.getEmail());
        assertEquals("mockJwtToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingEmail_throwsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ── Login Tests ───────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mockJwtToken");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("test@cinevault.com", response.getEmail());
        assertEquals("mockJwtToken", response.getToken());
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(authRequest)
        );

        verify(userRepository, never()).findByEmail(any());
    }
}
