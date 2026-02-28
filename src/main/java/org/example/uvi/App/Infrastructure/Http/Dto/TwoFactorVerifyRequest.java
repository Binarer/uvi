package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.NotNull;

public record TwoFactorVerifyRequest(
        @NotNull(message = "TOTP code is required")
        Integer code
) {}
