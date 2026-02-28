package org.example.uvi.App.Infrastructure.Http.Dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public AuthResponse(String accessToken) {
        this(accessToken, "Bearer", 86400);
    }
}
