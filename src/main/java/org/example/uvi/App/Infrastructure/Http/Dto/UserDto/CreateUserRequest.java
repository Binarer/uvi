package org.example.uvi.App.Infrastructure.Http.Dto.UserDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format")
        String phoneNumber,

        String username,
        LocalDate dateOfBirth,
        UserRole role,
        String city
) {}
