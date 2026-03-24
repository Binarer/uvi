package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.OsType.OsType;

public record DeviceRequest(
        @NotBlank(message = "Device token is required")
        String deviceToken,

        @NotNull(message = "OS type is required")
        OsType osType
) {}
