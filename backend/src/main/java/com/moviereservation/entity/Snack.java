package com.moviereservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "snacks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Snack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String description;

    private String emoji;

    private boolean available = true;

    @Enumerated(EnumType.STRING)
    private SnackSize size = SnackSize.MEDIUM;

    public enum SnackSize {
        SMALL, MEDIUM, LARGE
    }
}
