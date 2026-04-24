package com.moviereservation.controller;

import com.moviereservation.entity.Snack;
import com.moviereservation.repository.SnackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnackControllerTest {

    @Mock private SnackRepository snackRepository;

    @InjectMocks
    private SnackController snackController;

    private Snack snack;

    @BeforeEach
    void setUp() {
        snack = Snack.builder()
                .id(1L)
                .name("Popcorn")
                .price(new BigDecimal("5.99"))
                .description("Large salted popcorn")
                .emoji("🍿")
                .available(true)
                .size(Snack.SnackSize.LARGE)
                .build();
    }

    // ── Get Available Snacks Tests ─────────────────────────────────────────

    @Test
    void getAvailableSnacks_returnsOnlyAvailableSnacks() {
        when(snackRepository.findByAvailableTrue()).thenReturn(List.of(snack));

        ResponseEntity<List<Snack>> response = snackController.getAvailableSnacks();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).isAvailable());
    }

    @Test
    void getAvailableSnacks_whenNoSnacks_returnsEmptyList() {
        when(snackRepository.findByAvailableTrue()).thenReturn(List.of());

        ResponseEntity<List<Snack>> response = snackController.getAvailableSnacks();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    // ── Get All Snacks Tests ───────────────────────────────────────────────

    @Test
    void getAllSnacks_returnsAllSnacks() {
        Snack unavailableSnack = Snack.builder()
                .id(2L)
                .name("Hotdog")
                .price(new BigDecimal("3.99"))
                .available(false)
                .build();

        when(snackRepository.findAll()).thenReturn(List.of(snack, unavailableSnack));

        ResponseEntity<List<Snack>> response = snackController.getAllSnacks();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }

    // ── Create Snack Tests ────────────────────────────────────────────────

    @Test
    void createSnack_savesAndReturnsSnack() {
        when(snackRepository.save(any(Snack.class))).thenReturn(snack);

        ResponseEntity<Snack> response = snackController.createSnack(snack);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Popcorn", response.getBody().getName());
        verify(snackRepository).save(snack);
    }

    // ── Update Snack Tests ────────────────────────────────────────────────

    @Test
    void updateSnack_withValidId_updatesSuccessfully() {
        Snack updated = Snack.builder()
                .name("Large Popcorn")
                .price(new BigDecimal("6.99"))
                .description("Extra large salted popcorn")
                .emoji("🍿")
                .available(true)
                .size(Snack.SnackSize.LARGE)
                .build();

        when(snackRepository.findById(1L)).thenReturn(Optional.of(snack));
        when(snackRepository.save(any(Snack.class))).thenReturn(snack);

        ResponseEntity<Snack> response = snackController.updateSnack(1L, updated);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Large Popcorn", snack.getName());
        assertEquals(new BigDecimal("6.99"), snack.getPrice());
        verify(snackRepository).save(snack);
    }

    @Test
    void updateSnack_withInvalidId_throwsException() {
        when(snackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> snackController.updateSnack(99L, snack)
        );
    }

    // ── Delete Snack Tests ────────────────────────────────────────────────

    @Test
    void deleteSnack_withValidId_deletesSuccessfully() {
        ResponseEntity<Void> response = snackController.deleteSnack(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(snackRepository).deleteById(1L);
    }
}