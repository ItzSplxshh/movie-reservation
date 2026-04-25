package com.moviereservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for authentication responses.
 * Returned by both the register and login endpoints on success.
 * Contains the JWT token and user details needed by the React frontend
 * to authenticate subsequent requests and display user information.
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    /** Signed JWT token to be included in the Authorization header of subsequent requests */
    private String token;

    /** The authenticated user's email address */
    private String email;

    /** The authenticated user's full name displayed in the navigation bar */
    private String fullName;

    /** The authenticated user's role — USER, ADMIN or SUPER_ADMIN */
    private String role;
}