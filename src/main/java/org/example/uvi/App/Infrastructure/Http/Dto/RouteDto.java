package org.example.uvi.App.Infrastructure.Http.Dto;

import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;

import java.util.List;

public record RouteDto(
        Double startLat,
        Double startLon,
        Double endLat,
        Double endLon,
        RouteMode mode,
        Double totalDistanceMeters,
        Double totalDistanceKm,
        Integer totalSegments,
        List<RoutePointDto> points
) {}
