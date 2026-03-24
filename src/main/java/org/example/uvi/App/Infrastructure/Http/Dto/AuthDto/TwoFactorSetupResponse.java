package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing data to set up Google 2FA")
public record TwoFactorSetupResponse(
        @Schema(description = "QR code URL for scanning with an app")
        String qrCodeUrl,
        @Schema(description = "Secret key for manual entry if QR code fails", example = "JBSWY3DPEHPK3PXP")
        String manualEntryKey
) {}
