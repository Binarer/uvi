package org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.OsType.OsType;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Information about a user's registered device")
public record DeviceDto(
        @Schema(description = "Unique identifier of the device record")
        UUID id,
        @Schema(description = "ID of the user who owns the device")
        Long userId,
        @Schema(description = "FCM or APNS push token", example = "fcm_token_123...")
        String deviceToken,
        @Schema(description = "Operating system of the device", example = "ANDROID")
        OsType osType,
        @Schema(description = "Last time the device was active")
        Instant lastActiveAt
) {}
