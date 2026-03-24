package org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.OsType.OsType;

@Schema(description = "Request to register or update a device token")
public record RegisterDeviceRequest(
        @NotBlank(message = "Device token is required")
        @Schema(description = "FCM or APNS push token", example = "fcm_token_123...")
        String deviceToken,

        @NotNull(message = "OS type is required")
        @Schema(description = "Operating system of the device", example = "ANDROID")
        OsType osType
) {}