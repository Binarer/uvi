package org.example.uvi.App.Infrastructure.Http.Dto.UserDto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 100)
        String username,

        LocalDate dateOfBirth,
        String city,
        Double latitude,
        Double longitude
) {}
