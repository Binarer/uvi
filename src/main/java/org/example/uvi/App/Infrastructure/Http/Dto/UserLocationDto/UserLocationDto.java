package org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "User location update information")
public record UserLocationDto(
        @Schema(description = "Unique ID of the location record")
        UUID id,
        @Schema(description = "ID of the user")
        Long userId,
        @Schema(description = "Latitude coordinate", example = "56.8286")
        Double latitude,
        @Schema(description = "Longitude coordinate", example = "60.6033")
        Double longitude,
        @Schema(description = "GPS accuracy in meters", example = "5.0")
        Float accuracy,
        @Schema(description = "Device battery level percentage", example = "85")
        Integer batteryLevel,
        @Schema(description = "Speed in m/s", example = "1.2")
        Float speed,
        @Schema(description = "Timestamp of the location record")
        Instant timestamp
) {}
