package com.moviereservation.controller;

import com.moviereservation.entity.Snack;
import com.moviereservation.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackRepository snackRepository;

    // Public — anyone can view available snacks
    @GetMapping
    public ResponseEntity<List<Snack>> getAvailableSnacks() {
        return ResponseEntity.ok(snackRepository.findByAvailableTrue());
    }

    // Admin only
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Snack>> getAllSnacks() {
        return ResponseEntity.ok(snackRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Snack> createSnack(@RequestBody Snack snack) {
        return ResponseEntity.ok(snackRepository.save(snack));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Snack> updateSnack(@PathVariable Long id, @RequestBody Snack updated) {
        Snack snack = snackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snack not found"));
        snack.setName(updated.getName());
        snack.setPrice(updated.getPrice());
        snack.setDescription(updated.getDescription());
        snack.setEmoji(updated.getEmoji());
        snack.setAvailable(updated.isAvailable());
        snack.setSize(updated.getSize());
        return ResponseEntity.ok(snackRepository.save(snack));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSnack(@PathVariable Long id) {
        snackRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}