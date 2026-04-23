package com.moviereservation.controller;

import com.moviereservation.entity.*;
import com.moviereservation.repository.*;
import com.moviereservation.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final TheaterRepository theaterRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TheaterService theaterService;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Cannot modify a Super Admin account.");
        }
        user.setRole(User.Role.valueOf(body.get("role")));
        return ResponseEntity.ok(userRepository.save(user));
    }


    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == User.Role.SUPER_ADMIN) {
            return ResponseEntity.status(403).body("Cannot delete a Super Admin account.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/theaters")
    public ResponseEntity<List<Theater>> getTheaters() {
        return ResponseEntity.ok(theaterRepository.findAll());
    }

    @PostMapping("/theaters")
    public ResponseEntity<Theater> createTheater(@RequestBody Theater theater) {
        return ResponseEntity.ok(theaterService.createTheater(theater));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<List<Showtime>> getShowtimes() {
        return ResponseEntity.ok(showtimeRepository.findAll());
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        Map<String, Object> report = new HashMap<>();

        // All confirmed reservations
        List<Reservation> confirmed = reservationRepository
                .findAll()
                .stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
                .collect(Collectors.toList());

        // All reservations
        List<Reservation> all = reservationRepository.findAll();

        // Total revenue
        BigDecimal totalRevenue = confirmed.stream()
                .map(Reservation::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);

        // Total confirmed bookings
        report.put("totalBookings", confirmed.size());

        // Bookings today
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long bookingsToday = confirmed.stream()
                .filter(r -> r.getPaidAt() != null && r.getPaidAt().isAfter(startOfDay))
                .count();
        report.put("bookingsToday", bookingsToday);

        // Cancellation rate
        long cancelled = all.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED)
                .count();
        double cancellationRate = all.isEmpty() ? 0 :
                Math.round((double) cancelled / all.size() * 1000.0) / 10.0;
        report.put("cancellationRate", cancellationRate);

        // Most popular movie
        confirmed.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getShowtime().getMovie().getTitle(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> report.put("mostPopularMovie", e.getKey()));

        // Revenue per movie
        Map<String, BigDecimal> revenuePerMovie = confirmed.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getShowtime().getMovie().getTitle(),
                        Collectors.reducing(BigDecimal.ZERO,
                                Reservation::getTotalPrice,
                                BigDecimal::add)
                ));
        report.put("revenuePerMovie", revenuePerMovie);

        // Occupancy per showtime
        List<Map<String, Object>> occupancy = showtimeRepository.findAll().stream()
                .map(st -> {
                    long totalSeats = seatRepository.countByTheaterId(st.getTheater().getId());
                    long bookedSeats = confirmed.stream()
                            .filter(r -> r.getShowtime().getId().equals(st.getId()))
                            .mapToLong(r -> r.getSeats().size())
                            .sum();
                    double occupancyPct = totalSeats == 0 ? 0 :
                            Math.round((double) bookedSeats / totalSeats * 1000.0) / 10.0;

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("showtimeId", st.getId());
                    entry.put("movie", st.getMovie().getTitle());
                    entry.put("theater", st.getTheater().getName());
                    entry.put("startTime", st.getStartTime());
                    entry.put("totalSeats", totalSeats);
                    entry.put("bookedSeats", bookedSeats);
                    entry.put("availableSeats", totalSeats - bookedSeats);
                    entry.put("occupancyPct", occupancyPct);
                    return entry;
                })
                .collect(Collectors.toList());
        report.put("occupancy", occupancy);

        // Recent bookings
        List<Map<String, Object>> recentBookings = confirmed.stream()
                .sorted(Comparator.comparing(Reservation::getPaidAt).reversed())
                .limit(10)
                .map(r -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id", r.getId());
                    entry.put("user", r.getUser().getFirstName() + " " + r.getUser().getLastName());
                    entry.put("movie", r.getShowtime().getMovie().getTitle());
                    entry.put("seats", r.getSeats().size());
                    entry.put("total", r.getTotalPrice());
                    entry.put("paidAt", r.getPaidAt());
                    return entry;
                })
                .collect(Collectors.toList());
        report.put("recentBookings", recentBookings);

        return ResponseEntity.ok(report);
    }
}
