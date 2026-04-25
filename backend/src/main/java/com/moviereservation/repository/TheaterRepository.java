package com.moviereservation.repository;

import com.moviereservation.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Theater database operations.
 * Extends JpaRepository to provide standard CRUD operations
 * including save, findById, findAll and delete.
 * No custom query methods are required as all theatre queries
 * use the standard JpaRepository methods.
 */
public interface TheaterRepository extends JpaRepository<Theater, Long> {}