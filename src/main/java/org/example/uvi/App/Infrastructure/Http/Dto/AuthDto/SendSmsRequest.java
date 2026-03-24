package org.example.uvi.App.Infrastructure.Http.Dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to send SMS verification code")
public record SendSmsRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format. Expected: 79XXXXXXXXX")
        @Schema(description = "Phone number in MSISDN format (starts with 7 or 8, followed by 10 digits)", example = "79123456789")
        String phoneNumber
) {}
