package com.moviereservation.repository;

import com.moviereservation.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for Snack database operations.
 * Extends JpaRepository to provide standard CRUD operations.
 * Includes a custom query method to retrieve only available snacks
 * for display in the public booking flow.
 */
public interface SnackRepository extends JpaRepository<Snack, Long> {

    /**
     * Finds all snacks marked as available.
     * Used by the public snack endpoint to return only snacks
     * that should be visible to users in the snack selection popup.
     * Unavailable snacks are excluded without being deleted,
     * allowing admins to temporarily remove items from the booking flow.
     *
     * @return a list of all snacks where available is true
     */
    List<Snack> findByAvailableTrue();
}