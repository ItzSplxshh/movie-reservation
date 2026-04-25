package com.moviereservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object for user registration requests.
 * Contains all fields required to create a new user account.
 * Validated using Bean Validation annotations before reaching the controller.
 * All fields are mandatory and the password must be at least 6 characters.
 */
@Data
public class RegisterRequest {

    /** User's email address — must be a valid email format and not blank */
    @Email @NotBlank
    private String email;

    /** User's chosen password — must be at least 6 characters */
    @NotBlank @Size(min = 6)
    private String password;

    /** User's first name — must not be blank */
    @NotBlank
    private String firstName;

    /** User's last name — must not be blank */
    @NotBlank
    private String lastName;
}