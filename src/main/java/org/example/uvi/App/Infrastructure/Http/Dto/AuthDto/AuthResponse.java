package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT tokens or 2FA requirement")
public record AuthResponse(
        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "JWT Refresh Token", example = "a7b8c9d0...")
        String refreshToken,
        @Schema(description = "Type of token", example = "Bearer")
        String tokenType,
        @Schema(description = "Access token expiration in seconds", example = "3600")
        Long expiresIn,
        @Schema(description = "Flag indicating that 2FA is required for this account")
        Boolean twoFactorRequired,
        @Schema(description = "Temporary token to be used for 2FA verification")
        String twoFactorTempToken
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000L, false, null);
    }

    public static AuthResponse twoFactorRequired(String tempToken) {
        return new AuthResponse(null, null, "Bearer", null, true, tempToken);
    }
}
