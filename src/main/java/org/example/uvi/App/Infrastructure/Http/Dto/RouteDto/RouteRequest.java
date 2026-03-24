package org.example.uvi.App.Infrastructure.Http.Dto.RouteDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;

@Schema(description = "Request to calculate a route between two coordinates")
public record RouteRequest(
        @NotNull(message = "Start latitude is required")
        @DecimalMin(value = "-90.0", message = "Start latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Start latitude must be <= 90")
        @Schema(description = "Latitude of the starting point", example = "56.8286")
        Double startLat,

        @NotNull(message = "Start longitude is required")
        @DecimalMin(value = "-180.0", message = "Start longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Start longitude must be <= 180")
        @Schema(description = "Longitude of the starting point", example = "60.6033")
        Double startLon,

        @NotNull(message = "End latitude is required")
        @DecimalMin(value = "-90.0", message = "End latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "End latitude must be <= 90")
        @Schema(description = "Latitude of the destination point", example = "56.8389")
        Double endLat,

        @NotNull(message = "End longitude is required")
        @DecimalMin(value = "-180.0", message = "End longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "End longitude must be <= 180")
        @Schema(description = "Longitude of the destination point", example = "60.6057")
        Double endLon,

        /**
         * Режим маршрутизации. По умолчанию — DRIVING.
         */
        @Schema(description = "Routing mode (DRIVING, PEDESTRIAN, PUBLIC_TRANSPORT)", defaultValue = "DRIVING")
        RouteMode mode
) {
    public RouteRequest {
        if (mode == null) mode = RouteMode.DRIVING;
    }
}
