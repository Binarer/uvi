package org.example.uvi.App.Infrastructure.Http.Dto;

public record RecommendedRequest(
        double latitude,
        double longitude,
        double radiusMeters,
        int limit
) {}
