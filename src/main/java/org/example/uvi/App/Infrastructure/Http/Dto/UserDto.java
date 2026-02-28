package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String username,
        LocalDate dateOfBirth,
        UserRole role,
        UserStatus status,
        String city,
        Double latitude,
        Double longitude,
        Boolean phoneVerified,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {}
