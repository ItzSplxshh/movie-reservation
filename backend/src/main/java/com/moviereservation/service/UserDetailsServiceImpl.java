package com.moviereservation.service;

import com.moviereservation.entity.User;
import com.moviereservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Implementation of Spring Security's UserDetailsService interface.
 * Bridges the CineVault User entity with Spring Security's authentication
 * mechanism by loading user details from the database by email address.
 * Called by the AuthenticationManager during login and by JwtAuthFilter
 * during token validation on every authenticated request.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user's security details by their email address.
     * Retrieves the user from the database and constructs a Spring Security
     * UserDetails object containing the email, hashed password and granted authorities.
     * The user's role is prefixed with "ROLE_" as required by Spring Security's
     * role-based access control conventions — for example USER becomes ROLE_USER,
     * ADMIN becomes ROLE_ADMIN and SUPER_ADMIN becomes ROLE_SUPER_ADMIN.
     * These authorities are then used by @PreAuthorize and hasRole() expressions
     * throughout the application to enforce access control.
     *
     * @param email the email address to load user details for
     * @return a UserDetails object containing the user's credentials and authorities
     * @throws UsernameNotFoundException if no user exists with the given email address
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                // Prefix role with ROLE_ as required by Spring Security conventions
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
