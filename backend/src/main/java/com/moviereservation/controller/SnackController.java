package com.moviereservation.controller;

import com.moviereservation.entity.Snack;
import com.moviereservation.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for snack management.
 * The public GET endpoint returns only available snacks for the booking flow.
 * All admin endpoints are restricted to ADMIN and SUPER_ADMIN roles,
 * allowing full CRUD management of the cinema snack menu.
 */
@RestController
@RequestMapping("/api/snacks")
@RequiredArgsConstructor
public class SnackController {

    private final SnackRepository snackRepository;

    /**
     * Returns all snacks marked as available.
     * Publicly accessible — used by the snack selection popup
     * on the seat selection page before checkout.
     * Unavailable snacks are hidden from users without being deleted,
     * allowing admins to temporarily remove items from the booking flow.
     */
    @GetMapping
    public ResponseEntity<List<Snack>> getAvailableSnacks() {
        return ResponseEntity.ok(snackRepository.findByAvailableTrue());
    }

    /**
     * Returns all snacks including unavailable ones.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     * Used by the admin panel Snacks tab to display and manage
     * the full snack catalogue.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Snack>> getAllSnacks() {
        return ResponseEntity.ok(snackRepository.findAll());
    }

    /**
     * Creates a new snack in the system.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     *
     * @param snack the snack to create with name, price, emoji, size and availability
     * @return the created snack with its generated ID
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Snack> createSnack(@RequestBody Snack snack) {
        return ResponseEntity.ok(snackRepository.save(snack));
    }

    /**
     * Updates an existing snack by ID.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     * Updates all fields including the availability toggle which controls
     * whether the snack appears in the public booking flow.
     *
     * @param id      the ID of the snack to update
     * @param updated the updated snack details
     * @return the updated snack
     */
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

    /**
     * Deletes a snack permanently by ID.
     * Restricted to ADMIN and SUPER_ADMIN roles.
     * For temporary removal, use the availability toggle instead.
     * Returns 204 No Content on successful deletion.
     *
     * @param id the ID of the snack to delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteSnack(@PathVariable Long id) {
        snackRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}