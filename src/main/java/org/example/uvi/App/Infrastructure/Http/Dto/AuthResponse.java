package org.example.uvi.App.Infrastructure.Http.Dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Boolean twoFactorRequired,
        String twoFactorTempToken
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000L, false, null);
    }

    public static AuthResponse twoFactorRequired(String tempToken) {
        return new AuthResponse(null, null, "Bearer", null, true, tempToken);
    }
}
