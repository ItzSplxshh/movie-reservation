package com.moviereservation.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT authentication filter that intercepts every incoming HTTP request.
 * Extends OncePerRequestFilter to guarantee execution exactly once per request.
 * Validates the JWT token from the Authorization header and sets the
 * authenticated user in Spring Security's SecurityContext if valid.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Intercepts each request to extract and validate the JWT token.
     * If the token is valid, the authenticated user is set in the
     * SecurityContext, granting access to protected endpoints.
     * If no token is present or the token is invalid, the request
     * proceeds unauthenticated and Spring Security enforces access rules.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Extract the Authorization header from the incoming request
        String authHeader = request.getHeader("Authorization");

        // If no Bearer token is present, skip authentication and continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix to get the raw JWT token
        String token = authHeader.substring(7);
        String username;

        try {
            // Extract the username (email) from the token claims
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            // Token is malformed or expired — continue without authentication
            chain.doFilter(request, response);
            return;
        }

        // Only authenticate if username was extracted and no authentication exists yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token signature and expiry against the user details
            if (jwtUtil.validateToken(token, userDetails)) {

                // Create an authentication token with the user's authorities (roles)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                // Attach request details such as IP address to the authentication
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authenticated user in the SecurityContext for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain regardless of authentication outcome
        chain.doFilter(request, response);
    }
}
