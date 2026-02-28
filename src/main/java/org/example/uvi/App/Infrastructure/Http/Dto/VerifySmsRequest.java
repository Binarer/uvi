package org.example.uvi.App.Infrastructure.Http.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifySmsRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[78]\\d{10}$", message = "Invalid phone number format")
        String phoneNumber,

        @NotBlank(message = "Code is required")
        @Size(min = 6, max = 6, message = "Code must be exactly 6 digits")
        @Pattern(regexp = "\\d{6}", message = "Code must consist of 6 digits")
        String code
) {}
