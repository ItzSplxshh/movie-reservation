package com.moviereservation.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for JSON Web Token operations.
 * Handles JWT generation, claim extraction and token validation
 * using the JJWT library with HMAC-SHA signing.
 * The signing secret and expiration duration are injected from
 * application properties to keep credentials out of source code.
 */
@Component
public class JwtUtil {

    /** Secret key used to sign and verify JWT tokens — injected from application properties */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Token expiration duration in milliseconds — injected from application properties */
    @Value("${app.jwt.expiration}")
    private long expiration;

    /**
     * Derives the HMAC-SHA signing key from the configured secret string.
     * Called internally for both token generation and validation.
     *
     * @return a SecretKey derived from the configured secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a signed JWT token for the authenticated user.
     * The token subject is set to the user's email address, which is
     * later extracted by JwtAuthFilter to identify the user on each request.
     * The token is signed with HMAC-SHA and expires after the configured duration.
     *
     * @param userDetails the authenticated user to generate a token for
     * @return a signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username (email) from a JWT token.
     * Parses and verifies the token signature before extracting the subject claim.
     * Throws a JwtException if the token is malformed or the signature is invalid.
     *
     * @param token the JWT token string to extract the username from
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT token against the provided user details.
     * Checks that the token's subject matches the user's username
     * and that the token has not expired.
     * Returns false if the token is malformed, expired or the signature is invalid
     * rather than throwing an exception, allowing the filter chain to continue.
     *
     * @param token       the JWT token string to validate
     * @param userDetails the user details to validate the token against
     * @return true if the token is valid for the given user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            Date expDate = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            // Token is valid if username matches and expiry has not passed
            return username.equals(userDetails.getUsername()) && !expDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // Return false for any token parsing or validation failure
            return false;
        }
    }
}
