package org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto;

public record RecommendedRequest(
        double latitude,
        double longitude,
        double radiusMeters,
        int limit
) {}
