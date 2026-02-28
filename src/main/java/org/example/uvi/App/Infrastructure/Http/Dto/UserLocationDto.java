package org.example.uvi.App.Infrastructure.Http.Dto;

import java.time.Instant;
import java.util.UUID;

public record UserLocationDto(
        UUID id,
        Long userId,
        Double latitude,
        Double longitude,
        Float accuracy,
        Integer batteryLevel,
        Float speed,
        Instant timestamp
) {}
