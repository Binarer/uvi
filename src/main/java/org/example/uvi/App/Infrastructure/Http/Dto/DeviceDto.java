package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.OsType.OsType;

import java.time.Instant;
import java.util.UUID;

public record DeviceDto(
        UUID id,
        Long userId,
        String deviceToken,
        OsType osType,
        Instant lastActiveAt
) {}
