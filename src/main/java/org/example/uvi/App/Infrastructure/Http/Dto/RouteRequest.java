package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;

public record RouteRequest(
        @NotNull(message = "Start latitude is required")
        @DecimalMin(value = "-90.0", message = "Start latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Start latitude must be <= 90")
        Double startLat,

        @NotNull(message = "Start longitude is required")
        @DecimalMin(value = "-180.0", message = "Start longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Start longitude must be <= 180")
        Double startLon,

        @NotNull(message = "End latitude is required")
        @DecimalMin(value = "-90.0", message = "End latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "End latitude must be <= 90")
        Double endLat,

        @NotNull(message = "End longitude is required")
        @DecimalMin(value = "-180.0", message = "End longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "End longitude must be <= 180")
        Double endLon,

        /**
         * Режим маршрутизации. По умолчанию — DRIVING.
         */
        RouteMode mode
) {
    public RouteRequest {
        if (mode == null) mode = RouteMode.DRIVING;
    }
}
