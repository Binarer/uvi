package org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto;

import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;

public record NearbyRequest(
        double latitude,
        double longitude,
        double radiusMeters,
        PlaceType type
) {}
