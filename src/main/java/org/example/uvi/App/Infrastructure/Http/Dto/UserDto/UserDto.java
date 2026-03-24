package org.example.uvi.App.Infrastructure.Http.Dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "User profile information")
public record UserDto(
        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,
        @Schema(description = "First name", example = "Ivan")
        String firstName,
        @Schema(description = "Last name", example = "Ivanov")
        String lastName,
        @Schema(description = "Phone number (MSISDN format)", example = "+79123456789")
        String phoneNumber,
        @Schema(description = "Username for social features", example = "ivan_ekb")
        String username,
        @Schema(description = "Date of birth")
        LocalDate dateOfBirth,
        @Schema(description = "User role in the system")
        UserRole role,
        @Schema(description = "Current account status")
        UserStatus status,
        @Schema(description = "User's primary city", example = "Yekaterinburg")
        String city,
        @Schema(description = "Last known latitude", example = "56.8286")
        Double latitude,
        @Schema(description = "Last known longitude", example = "60.6033")
        Double longitude,
        @Schema(description = "Is phone number verified via SMS")
        boolean phoneVerified,
        @Schema(description = "Account registration timestamp")
        LocalDateTime createdAt,
        @Schema(description = "Last login timestamp")
        LocalDateTime lastLoginAt
) {}
