package com.moviereservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data Transfer Object for user login requests.
 * Contains the credentials required to authenticate an existing user.
 * Validated using Bean Validation annotations before reaching the controller.
 */
@Data
public class AuthRequest {

    /** User's email address — must be a valid email format and not blank */
    @Email @NotBlank
    private String email;

    /** User's password — must not be blank */
    @NotBlank
    private String password;
}