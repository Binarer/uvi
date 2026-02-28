package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Models.Device.Device;

import java.time.Instant;
import java.util.UUID;

public record DeviceDto(
        UUID id,
        Long userId,
        String deviceToken,
        Device.OsType osType,
        Instant lastActiveAt
) {}
