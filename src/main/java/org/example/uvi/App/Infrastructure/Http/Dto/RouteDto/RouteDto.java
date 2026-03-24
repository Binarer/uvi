package org.example.uvi.App.Infrastructure.Http.Dto.RouteDto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;

import java.util.List;

@Schema(description = "Calculated route information")
public record RouteDto(
        @Schema(description = "Starting latitude", example = "56.8286")
        Double startLat,
        @Schema(description = "Starting longitude", example = "60.6033")
        Double startLon,
        @Schema(description = "Ending latitude", example = "56.8389")
        Double endLat,
        @Schema(description = "Ending longitude", example = "60.6057")
        Double endLon,
        @Schema(description = "Transportation mode used", example = "DRIVING")
        RouteMode mode,
        @Schema(description = "Total distance in meters", example = "1500.5")
        Double totalDistanceMeters,
        @Schema(description = "Total distance in kilometers", example = "1.5")
        Double totalDistanceKm,
        @Schema(description = "Number of segments/points in the route", example = "42")
        Integer totalSegments,
        @Schema(description = "Ordered list of coordinates forming the route path")
        List<RoutePointDto> points
) {}
