package org.example.uvi.App.Infrastructure.Http.Dto;

public record TwoFactorSetupResponse(
        String qrCodeUrl,
        String manualEntryKey
) {}
