package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to verify a 2FA TOTP code")
public record TwoFactorVerifyRequest(
        @NotNull(message = "TOTP code is required")
        @Schema(description = "6-digit code from Google Authenticator", example = "123456")
        Integer code
) {}
