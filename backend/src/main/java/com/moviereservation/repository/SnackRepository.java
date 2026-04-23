package com.moviereservation.repository;

import com.moviereservation.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnackRepository extends JpaRepository<Snack, Long> {
    List<Snack> findByAvailableTrue();
}
