package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to verify SMS code")
public record VerifySmsRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format")
        @Schema(description = "Phone number in MSISDN format", example = "79123456789")
        String phoneNumber,

        @NotBlank(message = "Code is required")
        @Size(min = 6, max = 6, message = "Code must be exactly 6 digits")
        @Pattern(regexp = "\\d{6}", message = "Code must consist of 6 digits")
        @Schema(description = "6-digit verification code received via SMS", example = "123456")
        String code
) {}
