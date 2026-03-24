package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to refresh JWT access token")
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "Valid refresh token", example = "a7b8c9d0...")
        String refreshToken
) {}
